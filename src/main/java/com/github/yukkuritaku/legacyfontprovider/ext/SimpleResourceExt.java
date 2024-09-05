package com.github.yukkuritaku.legacyfontprovider.ext;

import java.io.BufferedReader;
import java.io.IOException;

public interface SimpleResourceExt {

    String legacyfontprovider$sourcePackId();

    BufferedReader legacyfontprovider$openAsReader();

    void legacyfontprovider$close() throws IOException;
}
