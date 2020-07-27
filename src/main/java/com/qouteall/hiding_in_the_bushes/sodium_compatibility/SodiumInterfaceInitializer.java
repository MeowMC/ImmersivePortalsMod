package com.qouteall.hiding_in_the_bushes.sodium_compatibility;

import com.qouteall.hiding_in_the_bushes.SodiumInterface;
import com.qouteall.immersive_portals.render.PixelCuller;
import me.jellysquid.mods.sodium.client.IWorldRenderer;
import me.jellysquid.mods.sodium.client.SodiumHooks;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderManager;

public class SodiumInterfaceInitializer {
    public static void init() {
        SodiumInterface.createClientChunkManager = ClientChunkManagerWithSodium::new;
        
        SodiumInterface.createNewRenderingContext = worldRenderer -> {
            SodiumWorldRenderer swr = ((IWorldRenderer) worldRenderer).getSodiumWorldRenderer();
            return swr.createNewRenderContext();
        };
        
        SodiumInterface.switchRenderingContext = (worldRenderer, newContext) -> {
            SodiumWorldRenderer sodiumWorldRenderer =
                ((IWorldRenderer) worldRenderer).getSodiumWorldRenderer();
            
            // must update render list
            sodiumWorldRenderer.scheduleTerrainUpdate();
            
            return sodiumWorldRenderer.switchRenderContext(((ChunkRenderManager.RenderContext) newContext));
        };
        
        SodiumHooks.shouldEnableCulling = () -> PixelCuller.isCullingEnabled;
        SodiumHooks.getCullingEquation = () -> {
            double[] doubles = PixelCuller.getActiveCullingPlaneEquation();
            float[] floats = new float[]{
                (float) doubles[0], (float) doubles[1], (float) doubles[2], (float) doubles[3]
            };
            return floats;
        };
    }
}
