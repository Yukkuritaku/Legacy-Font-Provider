package com.github.yukkuritaku.legacyfontprovider.mixin.normal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import net.minecraft.client.resources.SimpleResource;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.github.yukkuritaku.legacyfontprovider.ext.SimpleResourceExt;

@Mixin(SimpleResource.class)
public abstract class SimpleResourceMixin implements SimpleResourceExt {

    @Shadow
    public abstract InputStream getInputStream();

    @Shadow
    @Final
    private ResourceLocation srResourceLocation;

    @Shadow
    @Final
    private InputStream mcmetaInputStream;

    @Shadow
    @Final
    private InputStream resourceInputStream;

    @Override
    public String legacyfontprovider$sourcePackId() {
        return this.srResourceLocation.toString();
    }

    @Override
    public BufferedReader legacyfontprovider$openAsReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public void legacyfontprovider$close() throws IOException {
        this.resourceInputStream.close();
        if (this.mcmetaInputStream != null) {
            this.mcmetaInputStream.close();
        }
    }
}
