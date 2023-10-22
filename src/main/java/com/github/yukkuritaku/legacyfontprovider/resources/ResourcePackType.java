package com.github.yukkuritaku.legacyfontprovider.resources;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum ResourcePackType {
    CLIENT_RESOURCES("assets"),
    SERVER_DATA("data");

    private final String directoryName;

    ResourcePackType(String directoryNameIn) {
        this.directoryName = directoryNameIn;
    }

    public String getDirectoryName() {
        return this.directoryName;
    }
}
