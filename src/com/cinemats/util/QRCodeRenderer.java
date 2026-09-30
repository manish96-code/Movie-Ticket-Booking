package com.cinemats.util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class QRCodeRenderer {

    public static BufferedImage generateQRCode(String data, int size) {
        return renderQRCode(data, size, size);
    }

    public static BufferedImage renderQRCode(String data, int width, int height) {
        int matrixSize = 29; // Standard Version 3 QR dimension (29x29 modules)
        boolean[][] modules = new boolean[matrixSize][matrixSize];

        // 1. Draw 3 Corner Finder Patterns (7x7)
        drawFinderPattern(modules, 0, 0);
        drawFinderPattern(modules, matrixSize - 7, 0);
        drawFinderPattern(modules, 0, matrixSize - 7);

        // 2. Separators & Timing Patterns
        for (int i = 8; i < matrixSize - 8; i++) {
            modules[6][i] = (i % 2 == 0);
            modules[i][6] = (i % 2 == 0);
        }

        // 3. Alignment Pattern (5x5) at (20, 20)
        drawAlignmentPattern(modules, matrixSize - 9, matrixSize - 9);

        // 4. Data encoding simulation based on input string hash & characters
        byte[] bytes = (data != null ? data : "CINEMATS-TICKET").getBytes();
        int hash = 17;
        for (byte b : bytes) {
            hash = 31 * hash + (b & 0xFF);
        }

        long lfsr = (long) hash & 0xFFFFFFFFL;
        if (lfsr == 0) lfsr = 0xACE1;

        int byteIndex = 0;
        for (int r = 0; r < matrixSize; r++) {
            for (int c = 0; c < matrixSize; c++) {
                // Skip finder & timing patterns
                if (isReservedModule(r, c, matrixSize)) continue;

                // Simple pseudo-random linear feedback based on data
                lfsr = (lfsr >> 1) ^ (-(lfsr & 1L) & 0xD0000001L);
                byte b = bytes[byteIndex % bytes.length];
                boolean bit = ((b >> (byteIndex % 8)) & 1) == 1;
                modules[r][c] = ((lfsr & 1) == 1) ^ bit;
                byteIndex++;
            }
        }

        // Render to BufferedImage with quiet zone
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        int quietZone = 2; // modules
        int totalModules = matrixSize + 2 * quietZone;
        double cellSize = (double) Math.min(width, height) / totalModules;
        int offsetX = (int) ((width - totalModules * cellSize) / 2.0);
        int offsetY = (int) ((height - totalModules * cellSize) / 2.0);

        g2.setColor(Color.BLACK);
        for (int r = 0; r < matrixSize; r++) {
            for (int c = 0; c < matrixSize; c++) {
                if (modules[r][c]) {
                    int x = offsetX + (int) Math.round((c + quietZone) * cellSize);
                    int y = offsetY + (int) Math.round((r + quietZone) * cellSize);
                    int w = (int) Math.ceil(cellSize);
                    int h = (int) Math.ceil(cellSize);
                    g2.fillRect(x, y, w, h);
                }
            }
        }

        g2.dispose();
        return img;
    }

    private static void drawFinderPattern(boolean[][] modules, int startR, int startC) {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                if (r == 0 || r == 6 || c == 0 || c == 6) {
                    modules[startR + r][startC + c] = true;
                } else if (r >= 2 && r <= 4 && c >= 2 && c <= 4) {
                    modules[startR + r][startC + c] = true;
                } else {
                    modules[startR + r][startC + c] = false;
                }
            }
        }
    }

    private static void drawAlignmentPattern(boolean[][] modules, int startR, int startC) {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (r == 0 || r == 4 || c == 0 || c == 4 || (r == 2 && c == 2)) {
                    modules[startR + r][startC + c] = true;
                } else {
                    modules[startR + r][startC + c] = false;
                }
            }
        }
    }

    private static boolean isReservedModule(int r, int c, int size) {
        // Top-left finder + separator
        if (r <= 7 && c <= 7) return true;
        // Top-right finder + separator
        if (r <= 7 && c >= size - 8) return true;
        // Bottom-left finder + separator
        if (r >= size - 8 && c <= 7) return true;
        // Timing patterns
        if (r == 6 || c == 6) return true;
        // Alignment pattern
        if (r >= size - 9 && r <= size - 5 && c >= size - 9 && c <= size - 5) return true;
        return false;
    }
}

