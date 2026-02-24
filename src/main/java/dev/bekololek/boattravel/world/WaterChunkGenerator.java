package dev.bekololek.boattravel.world;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

/**
 * Generates a flat water world: bedrock at y=0, water y=1–62, open air above.
 * Water surface is at y=63 (sea level). Boats spawn at y=63.
 */
public class WaterChunkGenerator extends ChunkGenerator {

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random,
                              int chunkX, int chunkZ, ChunkData chunkData) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunkData.setBlock(x, 0, z, Material.BEDROCK);
                for (int y = 1; y < 63; y++) {
                    chunkData.setBlock(x, y, z, Material.WATER);
                }
                // y=63 left as water (sea level) — boats float here
                chunkData.setBlock(x, 63, z, Material.WATER);
            }
        }
    }

    @Override public boolean shouldGenerateDecorations() { return false; }
    @Override public boolean shouldGenerateMobs()        { return false; }
    @Override public boolean shouldGenerateStructures()  { return false; }
    @Override public boolean shouldGenerateSurface()     { return false; }
    @Override public boolean shouldGenerateCaves()       { return false; }
}
