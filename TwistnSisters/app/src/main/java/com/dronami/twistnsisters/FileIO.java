package com.dronami.twistnsisters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

public interface FileIO {
    public InputStream readAsset(String fileName) throws IOException;
    public InputStream readFile(String fileName) throws IOException;
    public OutputStream writeFile(String fileName) throws IOException;
}
