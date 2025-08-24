package com.argus.input;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.nio.ByteBuffer;

/**
 * Client mod that:
 *  - Reads the Argus engine native handle from JVM property: -Dargus.handle=<long>
 *  - Polls native input queue each tick
 *  - Injects into MC client safely
 *  - Draws a small "Argus" HUD button row (Skins/Marketplace/Close)
 */
public class ArgusInputShim implements ClientModInitializer {

    // Mirror the C layout offsets (see your argus_input_event_t)
    private static final int OFF_TYPE    =  8;      // int
    private static final int OFF_X       = 16;      // float (x)
    private static final int OFF_Y       = 20;      // float (y)
    private static final int OFF_DX      = 24;      // float (dx)
    private static final int OFF_DY      = 28;      // float (dy)
    private static final int OFF_BTN     = 32;      // int (buttons)
    private static final int OFF_KEY     = 40;      // int (keycode)
    private static final int OFF_ACTION  = 44;      // byte (action)
    private static final int OFF_MODS    = 45;      // byte (mods)

    private static final int TYPE_MOTION = 1;
    private static final int TYPE_KEY    = 3;

    private static long ENGINE_HANDLE = 0L;
    private static final ByteBuffer EVT = ArgusNative.newEventBuffer();

    private static float sensitivity = 1.0f;

    @Override
    public void onInitializeClient() {
        String prop = System.getProperty("argus.handle", "0");
        try { ENGINE_HANDLE = Long.parseUnsignedLong(prop); } catch (Exception ignored) {}

        // Pump events every client tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ENGINE_HANDLE == 0) return;
            while (ArgusNative.nativeDequeue(ENGINE_HANDLE, EVT) == 1) {
                int type = EVT.getInt(OFF_TYPE);
                if (type == TYPE_MOTION) {
                    float dx = EVT.getFloat(OFF_DX);
                    float dy = EVT.getFloat(OFF_DY);
                    int buttons = EVT.getInt(OFF_BTN);
                    ArgusMcBridge.injectMotion(dx, dy, buttons, sensitivity);
                } else if (type == TYPE_KEY) {
                    int key = EVT.getInt(OFF_KEY);
                    int action = EVT.get(OFF_ACTION);
                    int mods = EVT.get(OFF_MODS);
                    ArgusMcBridge.injectKey(key, action, mods);
                }
                EVT.rewind();
            }
        });

        // Minimal HUD (top bar)
        HudRenderCallback.EVENT.register(ArgusInputShim::drawHud);
    }

    private static void drawHud(DrawContext ctx, float tickDelta){
        var mc = MinecraftClient.getInstance();
        if (mc.currentScreen != null) return;

        int sw = ctx.getScaledWindowWidth();
        int barH = 18;
        ctx.fill(0, 0, sw, barH, 0xFF151515);
        for (int y=0; y<barH; y+=2) ctx.fill(0, y, sw, y+1, 0x20111111);

        // "X" close box
        int x0 = sw-18, y0=2;
        ctx.fill(x0, y0, x0+16, y0+16, 0xFF2B2B2B);
        ctx.drawText(mc.textRenderer, "X", x0+5, y0+4, 0xFFE1E1E1, false);

        // Buttons
        ctx.fill(6, 3, 56, 15, 0xFF2B2B2B);
        ctx.drawText(mc.textRenderer, "Skins", 12, 5, 0xFFFFFFFF, false);
        ctx.fill(62, 3, 152, 15, 0xFF2B2B2B);
        ctx.drawText(mc.textRenderer, "Marketplace", 68, 5, 0xFFFFFFFF, false);

        // Click hitboxes – simple/safe
        if (mc.mouse.wasLeftButtonClicked()){
            int mx = (int)(mc.mouse.getX() / mc.getWindow().getScaleFactor());
            int my = (int)(mc.mouse.getY() / mc.getWindow().getScaleFactor());
            if (my >= 2 && my <= 18){
                if (mx >= x0 && mx <= x0+16) {
                    mc.scheduleStop();
                } else if (mx >= 6 && mx <= 56) {
                    mc.setScreen(new net.minecraft.client.gui.screen.message.SocialInteractionsScreen(mc));
                } else if (mx >= 62 && mx <= 152) {
                    mc.setScreen(new net.minecraft.client.gui.screen.advancement.AdvancementsScreen(mc.player.networkHandler.getAdvancementHandler()));
                }
            }
        }
    }
}