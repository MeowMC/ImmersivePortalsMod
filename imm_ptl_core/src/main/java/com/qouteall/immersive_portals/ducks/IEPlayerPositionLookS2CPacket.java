package com.qouteall.immersive_portals.ducks;

import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public interface IEPlayerPositionLookS2CPacket {
    RegistryKey<World> getPlayerDimension();
    
    void setPlayerDimension(RegistryKey<World> dimension);
}
