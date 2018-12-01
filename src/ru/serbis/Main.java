package ru.serbis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        /*int a = 0;
        a |= 1 << 3;
        int b = (a >> 3) & 1;*/


        ByteBuffer bf = ByteBuffer.allocate(4);
        bf.order(ByteOrder.BIG_ENDIAN);
        bf.putInt(65534);
        byte b[] = bf.array();
        Runner runner = new Runner();
        runner.run();

    }
}
