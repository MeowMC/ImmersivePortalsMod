package com.qouteall.immersive_portals.portal.nether_portal;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalPlaceholderBlock;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;

import java.util.UUID;

public class NetherPortalEntity extends Portal {
    public static EntityType<NetherPortalEntity> entityType;
    
    //the reversed portal is in another dimension and face the opposite direction
    public UUID reversePortalId;
    public ObsidianFrame obsidianFrame;
    
    private boolean isNotified = true;
    private boolean shouldBreakNetherPortal = false;
    
    public NetherPortalEntity(
        EntityType type,
        World world
    ) {
        super(type, world);
    }
    
    public NetherPortalEntity(
        World world
    ) {
        super(entityType, world);
    }
    
    private void breakPortalOnThisSide() {
        assert shouldBreakNetherPortal;
        assert !removed;
        
        breakNetherPortalBlocks();
        this.remove();
    }
    
    private void breakNetherPortalBlocks() {
        ServerWorld world1 = McHelper.getServer().getWorld(dimension);
    
        obsidianFrame.boxWithoutObsidian.stream()
            .filter(
                blockPos -> world1.getBlockState(
                    blockPos
                ).getBlock() == PortalPlaceholderBlock.instance
            )
            .forEach(
                blockPos -> world1.setBlockState(
                    blockPos, Blocks.AIR.getDefaultState()
                )
            );
    }
    
    @Override
    public boolean isPortalValid() {
        return super.isPortalValid() &&
            reversePortalId != null &&
            obsidianFrame != null;
    }
    
    private void notifyToCheckIntegrity() {
        isNotified = true;
    }
    
    private NetherPortalEntity getReversePortal() {
        assert !world.isClient;
        
        ServerWorld world = getServer().getWorld(dimensionTo);
        if (world == null) {
            return null;
        }
        else {
            ChunkPos chunkPos = new ChunkPos(new BlockPos(destination));
            world.setChunkForced(
                chunkPos.x, chunkPos.z, true
            );
            world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);
        
            return (NetherPortalEntity) world.getEntity(reversePortalId);
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!world.isClient) {
            if (isNotified) {
                isNotified = false;
                checkPortalIntegrity();
            }
            if (shouldBreakNetherPortal) {
                breakPortalOnThisSide();
            }
        }
    }
    
    private void checkPortalIntegrity() {
        assert !world.isClient;
        
        if (!isPortalValid()) {
            remove();
            return;
        }
    
        if (!isPortalIntactOnThisSide()) {
            shouldBreakNetherPortal = true;
            NetherPortalEntity reversePortal = getReversePortal();
            if (reversePortal != null) {
                reversePortal.shouldBreakNetherPortal = true;
            }
        }
    }
    
    private boolean isPortalIntactOnThisSide() {
        assert McHelper.getServer() != null;
        
        return NetherPortalMatcher.isObsidianFrameIntact(
            world,
            obsidianFrame.normalAxis,
            obsidianFrame.boxWithoutObsidian
        )
            && isInnerPortalBlocksIntact(world, obsidianFrame);
    }
    
    //if the region is not loaded, it will return true
    private static boolean isObsidianFrameIntact(
        DimensionType dimension,
        ObsidianFrame obsidianFrame
    ) {
        ServerWorld world = McHelper.getServer().getWorld(dimension);
        
        if (world == null) {
            return true;
        }
    
        if (!world.isChunkLoaded(obsidianFrame.boxWithoutObsidian.l)) {
            return true;
        }
    
        if (!NetherPortalMatcher.isObsidianFrameIntact(
            world,
            obsidianFrame.normalAxis,
            obsidianFrame.boxWithoutObsidian
        )) {
            return false;
        }
    
        return isInnerPortalBlocksIntact(world, obsidianFrame);
    }
    
    private static boolean isInnerPortalBlocksIntact(
        IWorld world,
        ObsidianFrame obsidianFrame
    ) {
        return obsidianFrame.boxWithoutObsidian.stream().allMatch(
            blockPos -> world.getBlockState(blockPos).getBlock()
                == PortalPlaceholderBlock.instance
        );
    }
    
    
    @Override
    protected void readCustomDataFromTag(CompoundTag compoundTag) {
        super.readCustomDataFromTag(compoundTag);
    
        reversePortalId = compoundTag.getUuid("reversePortalId");
        obsidianFrame = ObsidianFrame.fromTag(compoundTag.getCompound("obsidianFrame"));
    }
    
    @Override
    protected void writeCustomDataToTag(CompoundTag compoundTag) {
        super.writeCustomDataToTag(compoundTag);
    
        compoundTag.putUuid("reversePortalId", reversePortalId);
        compoundTag.put("obsidianFrame", obsidianFrame.toTag());
    }
    
}
