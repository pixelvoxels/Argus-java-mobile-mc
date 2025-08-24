package com.argus.input;

import net.minecraft.client.MinecraftClient;

/** Minimal, safe input injection helpers (1.20.1). */
public final class ArgusMcBridge {
    private ArgusMcBridge(){}

    /** Mouse-look + basic mouse buttons. */
    public static void injectMotion(float dx, float dy, int buttons, float sensitivity){
        var mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.isPaused()) return;

        var p = mc.player;
        p.setYaw(p.getYaw() + dx * sensitivity * 0.15f);
        float newPitch = p.getPitch() - dy * sensitivity * 0.15f;
        if (newPitch > 90f) newPitch = 90f;
        if (newPitch < -90f) newPitch = -90f;
        p.setPitch(newPitch);

        // Buttons: 1 = attack, 2 = use (bitfield)
        if ((buttons & 1) != 0) mc.doAttack();
        if ((buttons & 2) != 0) mc.doItemUse();
    }

    /** Discrete keys (jump, etc.). action: 1=down */
    public static void injectKey(int keyCode, int action, int mods){
        var mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        switch (keyCode){
            case 32: // Space -> jump
                if (action == 1) mc.player.jump();
                break;
            case 1001: // attack
                if (action == 1) mc.doAttack();
                break;
            case 1002: // use
                if (action == 1) mc.doItemUse();
                break;
            default:
                break;
        }
    }
}