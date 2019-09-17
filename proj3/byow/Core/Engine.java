package byow.Core;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;
import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    private static final int WIDTH = 80;
    private static final int HEIGHT = 30;
    private static Random RANDOM = null;
    private static ArrayList<Room> roomTracker = new ArrayList<Room>();
    private static int avatarX;
    private static int avatarY;
    private static int goalX;
    private static int goalY;
    private ArrayList<Character> inputTracker;
    private long seed;
    private boolean off;
    private File saveFile;
    private double mouseX;
    private double mouseY;
    private class Room {
        int xCenter;
        int yCenter;
        int xStartPoint;
        int yStartPoint;
        int height;
        int width;
        private Room(int height1, int width1, int xStart, int yStart) {
            height = height1;
            width = width1;
            xStartPoint = xStart;
            yStartPoint = yStart;
            xCenter = xStart + width / 2;
            yCenter = yStart + height / 2;
        }
    }
    private class KeyBoardInputSource {
        private static final boolean PRINT_TYPED_KEYS = false;
        private KeyBoardInputSource() {
            Font title = new Font("Monaco", Font.BOLD, 50);
            Font subtitle = new Font("Monaco", Font.PLAIN, 30);
            StdDraw.setFont(title);
            StdDraw.text(0.5, 0.8, "CS61B:The Game");
            StdDraw.setFont(subtitle);
            StdDraw.text(0.5, 0.6, "New Game (N)");
            StdDraw.text(0.5, 0.5, "Load Game (L)");
            StdDraw.text(0.5, 0.4, "Quit (Q)");
        }
        private char getNextKey() {
            while (true) {
                if (StdDraw.hasNextKeyTyped()) {
                    char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                    if (PRINT_TYPED_KEYS) {
                        System.out.print(c);
                    }
                    return c;
                }
            }
        }
        private boolean possibleNextInput() {
            return true;
        }
    }
    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    /**
     * save and load: until the user puts :q, store all the characters the user puts into the system
     * and save the txt file containing all the characters
     * when l is called, restore the state using the txt file
     */
    public void interactWithKeyboard() {
        boolean[][] occupied = new boolean[WIDTH][HEIGHT];
        int[][] types = new int[WIDTH][HEIGHT];
        Random ranDom;
        KeyBoardInputSource inputSource = new KeyBoardInputSource();
        String input = "";
        inputTracker = new ArrayList<Character>();
        while (inputSource.possibleNextInput()) {
            char c = Character.toUpperCase(inputSource.getNextKey());
            while (c != 'N' && c != 'L') {
                c = Character.toUpperCase(inputSource.getNextKey());
            }
            // When user tries to start a new game
            if (c == 'N') {
                inputTracker.add(c);
                promptsForSeed();
                char d = Character.toUpperCase(inputSource.getNextKey());
                while (d != 'S') {
                    inputTracker.add(d);
                    d = Character.toUpperCase(inputSource.getNextKey());
                }
                inputTracker.add(d);
                for (char a : inputTracker) {
                    String temp2 = Character.toString(a);
                    input += temp2;
                }
                long seed1 = findSeedFromInput(input);
                ranDom = new Random(seed1);
                ter.initialize(WIDTH, HEIGHT);
                drawTheWorld(occupied, types, seed1);
                hud(intArrayToTETileArray(types));
                determineDirectionAndMove(inputSource, types, inputTracker);
            }
            if (c == 'L') {
                String temp = load();
                TETile[][] temp2 = interactWithInputString(temp);
                ter.renderFrame(temp2);
                hud(temp2);
                for (int x = 0; x < temp2.length; x++) {
                    for (int y = 0; y < temp2[0].length; y++) {
                        types[x][y] = tileToInt(temp2[x][y]);
                    }
                }
                determineDirectionAndMove(inputSource, types, inputTracker);
            }
        }
    }
    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        inputTracker = new ArrayList<Character>();
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        int spot = 0;
        while (Character.toUpperCase(input.charAt(spot)) != 'N'
                && Character.toUpperCase(input.charAt(spot)) != 'L') {
            spot += 1;
            input = input.substring(spot);
        }
        if (input.startsWith("L") || input.startsWith("l")) {
            String temp = load();
            return interactWithInputString(temp);
        }
        //first find the seed
        long seed1 = findSeedFromInput(input);
        String seedTemp = Long.toString(seed1);
        inputTracker.add('N');
        for (int i = 0; i < Long.toString(seed1).length(); i++) {
            inputTracker.add(seedTemp.charAt(i));
        }
        inputTracker.add('S');
        //moving process
        boolean[][] occupied = new boolean[WIDTH][HEIGHT];
        int[][] types = new int[WIDTH][HEIGHT];
        finalWorldFrame = returnTheWorld(occupied, types, seed1);
        ter.initialize(WIDTH, HEIGHT);
        int start = inputTracker.size();
        if (start > input.length() - 1) {
            //rendering process
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    finalWorldFrame[x][y] = intToTileType(types[x][y]);
                }
            }
            return finalWorldFrame;
        }
        char e = Character.toUpperCase(input.charAt(start));
        while (!this.off) {
            determineDirectionAndMove(e, input, types, inputTracker, start);
        }
        //rendering process
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                finalWorldFrame[x][y] = intToTileType(types[x][y]);
            }
        }
        return finalWorldFrame;
    }
    //find the SEED number from the given input(N123456S)
    private long findSeedFromInput(String input) {
        long seed1 = 0;
        String temp4 = "";
        int start = 0;
        while (Character.toUpperCase(input.charAt(start)) != 'N'
                && Character.toUpperCase(input.charAt(start)) != 'L') {
            start += 1;
        }
        if (Character.toUpperCase(input.charAt(start)) == 'N') {
            input = input.substring(start);
            int temp = 0;
            int locationTemp = 1;
            int lengthOfSeed = 0;
            while (input.charAt(locationTemp) != 's' && input.charAt(locationTemp) != 'S') {
                lengthOfSeed += 1;
                locationTemp += 1;
            }
            //then compute SEED
            int location = 1;
            for (int i = 0; i < lengthOfSeed; i++) {
                char temp1 = input.charAt(location);
                String temp12 = Character.toString(temp1);
                location += 1;
                temp4 += temp12;
            }
            seed1 = Long.valueOf(temp4);
        } else if (Character.toUpperCase(input.charAt(start)) == 'L') {
            seed1 = this.seed;
        }
        return seed1;
    }
    //give an appropriate tile type for an integer
    private static TETile intToTileType(int input) {
        if (input == 0) {
            return Tileset.NOTHING;
        } else if (input == 1) {
            return Tileset.FLOOR;
        } else if (input == 2) {
            return Tileset.WALL;
        } else if (input == 3) {
            return Tileset.AVATAR;
        } else if (input == 4) {
            return Tileset.FLOWER;
        } else {
            return Tileset.NOTHING;
        }
    }
    private void putTheFirstRoom(int[][] target, boolean[][] occupied) {
        Room temp = firstRoomGenerator();
        // put the floors in the world
        for (int x = temp.xStartPoint; x < temp.xStartPoint + temp.width; x++) {
            for (int y = temp.yStartPoint; y < temp.yStartPoint + temp.height; y++) {
                target[x][y] = 1;
                occupied[x][y] = true;
            }
        }
        // put the walls around the room
        for (int xPosition = temp.xStartPoint; xPosition
                <= temp.xStartPoint + temp.width; xPosition++) {
            target[xPosition][temp.yStartPoint] = 2;
            target[xPosition][temp.yStartPoint + temp.height] = 2;
        }
        for (int yPosition = temp.yStartPoint; yPosition
                <= temp.yStartPoint + temp.height; yPosition++) {
            target[temp.xStartPoint][yPosition] = 2;
            target[temp.xStartPoint + temp.width][yPosition] = 2;
        }
        target[temp.xCenter][temp.yCenter] = 3;
        avatarX = temp.xCenter;
        avatarY = temp.yCenter;
        roomTracker.add(temp);
    }
    private void putAnotherRoom(Room room1, int[][] target, boolean[][] occupied) {
        //guaranteeing that the StartingPoint is at the right place
        int height = room1.height;
        int width = room1.width;
        int yStart = room1.yStartPoint;
        int xStart = room1.xStartPoint;
        int xStop = xStart + width;
        int yStop = yStart + height;
        //then check if the rooms are overlapped
        for (int xPosition = xStart; xPosition < xStop; xPosition++) {
            for (int yPosition = yStart; yPosition < yStop; yPosition++) {
                if (occupied[xPosition][yPosition]) {
                    return;
                }
            }
        }
        //if not, put the room in the world
        for (int xPosition = xStart; xPosition <= xStop; xPosition++) {
            for (int yPosition = yStart; yPosition <= yStop; yPosition++) {
                target[xPosition][yPosition] = 1;
                occupied[xPosition][yPosition] = true;
            }
        }
        // put the walls around the room
        for (int xPosition = xStart; xPosition <= xStop; xPosition++) {
            target[xPosition][yStart] = 2;
            target[xPosition][yStop] = 2;
            occupied[xPosition][yStart] = true;
            occupied[xPosition][yStop] = true;
        }
        for (int yPosition = yStart; yPosition <= yStop; yPosition++) {
            target[xStart][yPosition] = 2;
            target[xStop][yPosition] = 2;
            occupied[xStart][yPosition] = true;
            occupied[xStop][yPosition] = true;
        }
        roomTracker.add(room1);
    }
    private void buildHallways(Room room1, Room room2, int[][] target, boolean[][] occupied) {
        int xstart = room1.xCenter;
        int ystart = room1.yCenter;
        int xstop = room2.xCenter;
        int ystop = room2.yCenter;
        int xlength = Math.abs(xstop - xstart);
        int ylength = Math.abs(ystop - ystart);
        Room startRoom = findStartRoom(room1, room2);
        Room endRoom = findEndRoom(room1, room2);
        if (xlength != 0 && ylength != 0) {
            if (endRoom.yCenter > startRoom.yCenter) {
                for (int x = startRoom.xCenter + startRoom.width / 2; x < endRoom.xCenter; x++) {
                    occupied[x][startRoom.yCenter] = true;
                    occupied[x][startRoom.yCenter - 1] = true;
                    occupied[x][startRoom.yCenter + 1] = true;
                    target[x][startRoom.yCenter] = 1;
                    target[x][startRoom.yCenter - 1] = 2;
                    target[x][startRoom.yCenter + 1] = 2;
                }
                for (int y = startRoom.yCenter; y <= endRoom.yCenter - endRoom.height / 2; y++) {
                    occupied[startRoom.xCenter - 1][y] = true;
                    occupied[startRoom.xCenter + 1][y] = true;
                    occupied[startRoom.xCenter][y] = true;
                    target[endRoom.xCenter - 1][y] = 2;
                    target[endRoom.xCenter + 1][y] = 2;
                    target[endRoom.xCenter][y] = 1;
                }
                occupied[endRoom.xCenter][startRoom.yCenter - 1] = true;
                occupied[endRoom.xCenter + 1][startRoom.yCenter - 1] = true;
                target[endRoom.xCenter + 1][startRoom.yCenter - 1] = 2;
                target[endRoom.xCenter - 1][startRoom.yCenter] = 1;
                target[endRoom.xCenter][startRoom.yCenter - 1] = 2;
            } else {
                for (int x = startRoom.xCenter + startRoom.width / 2; x < endRoom.xCenter; x++) {
                    occupied[x][startRoom.yCenter] = true;
                    occupied[x][startRoom.yCenter - 1] = true;
                    occupied[x][startRoom.yCenter + 1] = true;
                    target[x][startRoom.yCenter] = 1;
                    target[x][startRoom.yCenter - 1] = 2;
                    target[x][startRoom.yCenter + 1] = 2;
                }
                for (int y = endRoom.yCenter + endRoom.height / 2; y <= startRoom.yCenter; y++) {
                    occupied[endRoom.xCenter - 1][y] = true;
                    occupied[endRoom.xCenter + 1][y] = true;
                    occupied[endRoom.xCenter][y] = true;
                    target[endRoom.xCenter - 1][y] = 2;
                    target[endRoom.xCenter + 1][y] = 2;
                    target[endRoom.xCenter][y] = 1;
                }
                occupied[endRoom.xCenter][startRoom.yCenter + 1] = true;
                occupied[endRoom.xCenter + 1][startRoom.yCenter + 1] = true;
                target[endRoom.xCenter + 1][startRoom.yCenter + 1] = 2;
                target[endRoom.xCenter][startRoom.yCenter + 1] = 2;
                target[endRoom.xCenter - 1][startRoom.yCenter] = 1;
            }
        } else if (xlength == 0 && ylength != 0) {
            for (int y = endRoom.yCenter + endRoom.height / 2;
                 y <= startRoom.yCenter - startRoom.height / 2; y++) {
                occupied[xstart - 1][y] = true;
                occupied[xstart + 1][y] = true;
                occupied[xstart][y] = true;
                target[xstart - 1][y] = 2;
                target[xstart + 1][y] = 2;
                target[xstart][y] = 1;
            }
        } else {
            for (int x = startRoom.xCenter + startRoom.width / 2;
                 x <= endRoom.xCenter - endRoom.width / 2; x++) {
                occupied[x][ystop] = true;
                occupied[x][ystop - 1] = true;
                occupied[x][ystop + 1] = true;
                target[x][ystop] = 1;
                target[x][ystop - 1] = 2;
                target[x][ystop + 1] = 2;
            }
        }
    }
    private boolean checkIfOkToBuildHallways(Room room1, Room room2, boolean[][] occupied) {
        int xstart = room1.xCenter;
        int ystart = room1.yCenter;
        int xstop = room2.xCenter;
        int ystop = room2.yCenter;
        int xlength = Math.abs(xstop - xstart);
        int ylength = Math.abs(ystop - ystart);
        Room startRoom = findStartRoom(room1, room2);
        Room endRoom = findEndRoom(room1, room2);
        if (startRoom.xCenter + startRoom.width / 2 > endRoom.xCenter - endRoom.width / 2) {
            return false;
        }
        if (xlength != 0 && ylength != 0) {
            if (endRoom.yCenter > startRoom.yCenter) {
                for (int x = startRoom.xCenter + startRoom.width / 2 + 1;
                     x < endRoom.xCenter; x++) {
                    if (occupied[x][startRoom.yCenter] || occupied[x][startRoom.yCenter - 1]
                            || occupied[x][startRoom.yCenter + 1]) {
                        return false;
                    }
                }
                for (int y = startRoom.yCenter; y <= endRoom.yCenter - endRoom.height / 2 - 1;
                     y++) {
                    if (occupied[startRoom.xCenter - 1][y] || occupied[startRoom.xCenter + 1][y]
                            || occupied[startRoom.xCenter][y]) {
                        return false;
                    }
                }
                if (occupied[endRoom.xCenter][startRoom.yCenter - 1]) {
                    return false;
                }
                if (occupied[endRoom.xCenter + 1][startRoom.yCenter - 1]) {
                    return false;
                }
                return true;
            } else {
                for (int x = startRoom.xCenter + startRoom.width / 2 + 1;
                     x < endRoom.xCenter; x++) {
                    if (occupied[x][startRoom.yCenter] || occupied[x][startRoom.yCenter - 1]
                            || occupied[x][startRoom.yCenter + 1]) {
                        return false;
                    }
                }
                for (int y = endRoom.yCenter + endRoom.height / 2 + 1;
                     y <= startRoom.yCenter; y++) {
                    if (occupied[endRoom.xCenter - 1][y] || occupied[endRoom.xCenter + 1][y]
                            || occupied[endRoom.xCenter][y]) {
                        return false;
                    }
                }
                if (occupied[endRoom.xCenter][startRoom.yCenter + 1]) {
                    return false;
                }
                if (occupied[endRoom.xCenter + 1][startRoom.yCenter + 1]) {
                    return false;
                }
                return true;
            }
        } else if (xlength == 0 && ylength != 0) {
            for (int y = endRoom.yCenter + endRoom.height / 2 + 1;
                 y < startRoom.yCenter - startRoom.height / 2; y++) {
                if (occupied[xstart - 1][y] || occupied[xstart + 1][y] || occupied[xstart][y]) {
                    return false;
                }
            }
            return true;
        } else {
            for (int x = startRoom.xCenter + startRoom.width / 2 + 1;
                 x < endRoom.xCenter - endRoom.width / 2; x++) {
                if (occupied[x][ystop] || occupied[x][ystop - 1] || occupied[x][ystop + 1]) {
                    return false;
                }
            }
            return true;
        }
    }
    private Room firstRoomGenerator() {
        // making sure that the first room is located on the right side
        int width = RANDOM.nextInt(10);
        while (width < 3) {
            width = RANDOM.nextInt(10);
        }
        int height = RANDOM.nextInt(10);
        while (height < 3) {
            height = RANDOM.nextInt(10);
        }
        //guaranteeing that the StartingPoint is at the righter position
        int xStart = RANDOM.nextInt(WIDTH);
        int xStop = xStart + width;
        while (xStop > WIDTH - 2 || xStop < WIDTH - 10) {
            xStart = RANDOM.nextInt(WIDTH);
            xStop = xStart + width;
        }
        int yStart = RANDOM.nextInt(HEIGHT);
        int yStop = yStart + height;
        while (yStop > HEIGHT - 2) {
            yStart = RANDOM.nextInt(HEIGHT);
            yStop = yStart + height;
        }
        Room random = new Room(height, width, xStart, yStart);
        return random;
    }
    private Room randomRoomGenerator() {
        // making sure that the size of the room is bigger the 3 x 3
        int width = RANDOM.nextInt(10);
        while (width < 3) {
            width = RANDOM.nextInt(10);
        }
        int height = RANDOM.nextInt(10);
        while (height < 3) {
            height = RANDOM.nextInt(10);
        }
        //guaranteeing that the StartingPoint is at the right place
        int xStart = RANDOM.nextInt(WIDTH);
        int xStop = xStart + width;
        while (xStop > WIDTH - 2) {
            xStart = RANDOM.nextInt(WIDTH);
            xStop = xStart + width;
        }
        int yStart = RANDOM.nextInt(HEIGHT);
        int yStop = yStart + height;
        while (yStop > HEIGHT - 2) {
            yStart = RANDOM.nextInt(HEIGHT);
            yStop = yStart + height;
        }
        Room random = new Room(height, width, xStart, yStart);
        return random;
    }
    private boolean ifRoomOkay(Room room1, boolean[][] occupied) {
        for (int xPosition = room1.xStartPoint; xPosition
                < room1.xStartPoint + room1.width; xPosition++) {
            for (int yPosition = room1.yStartPoint; yPosition
                    < room1.yStartPoint + room1.height; yPosition++) {
                if (occupied[xPosition][yPosition]) {
                    return false;
                }
            }
        }
        return true;
    }
    private boolean ifRoomCloseEnough(Room room1, Room room2) {
        int room1X = room1.xCenter;
        int room1Y = room1.yCenter;
        int room2X = room2.xCenter;
        int room2Y = room2.yCenter;
        return (Math.sqrt(Math.pow(Math.abs(room1X - room2X), 2)
                + Math.pow(Math.abs(room1Y - room2Y), 2)) < WIDTH / 5);
    }
    private Room findEndRoom(Room room1, Room room2) {
        int xstart = room1.xCenter;
        int xstop = room2.xCenter;
        if (xstart < xstop) {
            return room2;
        } else if (xstart > xstop) {
            return room1;
        } else {
            if (room1.yCenter > room2.yCenter) {
                return room2;
            } else {
                return room1;
            }
        }
    }
    private Room findStartRoom(Room room1, Room room2) {
        int xstart = room1.xCenter;
        int xstop = room2.xCenter;
        if (xstart < xstop) {
            return room1;
        } else if (xstart > xstop) {
            return room2;
        } else {
            if (room1.yCenter > room2.yCenter) {
                return room1;
            } else {
                return room2;
            }
        }
    }
    private void drawTheWorld(boolean[][] occupied1, int[][] target1, long seed1) {
        long seed2 = seed1;
        RANDOM = new Random(seed2);
        boolean[][] occupied = occupied1;
        int[][] types = target1;
        putTheFirstRoom(types, occupied);
        // put other rooms into the world
        for (int j = 0; j < 100; j++) {
            for (int i = 1; i < 1000; i++) {
                Room temporary = randomRoomGenerator();
                if (checkIfOkToBuildHallways(roomTracker.get(roomTracker.size() - 1),
                        temporary, occupied)
                        && ifRoomOkay(temporary, occupied)
                        && ifRoomCloseEnough(temporary,
                        roomTracker.get(roomTracker.size() - 1))) {
                    putAnotherRoom(temporary, types, occupied);
                    buildHallways(roomTracker.get(roomTracker.size() - 2),
                            temporary, types, occupied);
                    break;
                }
            }
        }
        // initialize tiles
        goalX = roomTracker.get(roomTracker.size() - 1).xCenter;
        goalY = roomTracker.get(roomTracker.size() - 1).yCenter;
        types[goalX][goalY] = 4;
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                finalWorldFrame[x][y] = Tileset.NOTHING;
            }
        }
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                finalWorldFrame[x][y] = intToTileType(types[x][y]);
            }
        }
        ter.renderFrame(finalWorldFrame);
        StdDraw.show();
    }
    private TETile[][] returnTheWorld(boolean[][] occupied1, int[][] target1, long seed1) {
        long seed2 = seed1;
        RANDOM = new Random(seed2);
        boolean[][] occupied = occupied1;
        int[][] types = target1;
        putTheFirstRoom(types, occupied);
        // put other rooms into the world
        for (int j = 0; j < 100; j++) {
            for (int i = 1; i < 1000; i++) {
                Room temporary = randomRoomGenerator();
                if (checkIfOkToBuildHallways(roomTracker.get(roomTracker.size() - 1),
                        temporary, occupied)
                        && ifRoomOkay(temporary, occupied)
                        && ifRoomCloseEnough(temporary,
                        roomTracker.get(roomTracker.size() - 1))) {
                    putAnotherRoom(temporary, types, occupied);
                    buildHallways(roomTracker.get(roomTracker.size() - 2),
                            temporary, types, occupied);
                    break;
                }
            }
        }
        // initialize tiles
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                finalWorldFrame[x][y] = Tileset.NOTHING;
            }
        }
        goalX = roomTracker.get(roomTracker.size() - 1).xCenter;
        goalY = roomTracker.get(roomTracker.size() - 1).yCenter;
        types[goalX][goalY] = 4;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                finalWorldFrame[x][y] = intToTileType(types[x][y]);
            }
        }
        return finalWorldFrame;
    }
    private void swap(int startX, int startY, int targetX, int targetY, int[][] target) {
        int temp = target[startX][startY];
        target[startX][startY] = target[targetX][targetY];
        target[targetX][targetY] = temp;
    }
    private void moveUp(int startX, int startY, int[][] target) {
        if (target[startX][startY + 1] == 2) {
            return;
        }
        if (target[startX][startY + 1] == 4) {
            target[startX][startY] = 1;
            target[startX][startY + 1] = 3;
            avatarY = startY + 1;
            win();
            ter.renderFrame(intArrayToTETileArray(target));
        } else {
            swap(startX, startY, startX, startY + 1, target);
            avatarY = startY + 1;
            ter.renderFrame(intArrayToTETileArray(target));
        }
    }
    private void moveLeft(int startX, int startY, int[][] target) {
        if (target[startX - 1][startY] == 2) {
            return;
        }
        if (target[startX - 1][startY] == 4) {
            target[startX][startY] = 1;
            target[startX - 1][startY] = 3;
            avatarX = startX - 1;
            win();
            ter.renderFrame(intArrayToTETileArray(target));
        } else {
            swap(startX, startY, startX - 1, startY, target);
            avatarX = startX - 1;
            ter.renderFrame(intArrayToTETileArray(target));
        }
    }
    private void moveRight(int startX, int startY, int[][] target) {
        if (target[startX + 1][startY] == 2) {
            return;
        }
        if (target[startX + 1][startY] == 4) {
            target[startX][startY] = 1;
            target[startX + 1][startY] = 3;
            avatarX = startX + 1;
            win();
            ter.renderFrame(intArrayToTETileArray(target));
        } else {
            swap(startX, startY, startX + 1, startY, target);
            avatarX = startX + 1;
            ter.renderFrame(intArrayToTETileArray(target));
        }
    }
    private void moveDown(int startX, int startY, int[][] target) {
        if (target[startX][startY - 1] == 2) {
            return;
        }
        if (target[startX][startY - 1] == 4) {
            target[startX][startY] = 1;
            target[startX][startY - 1] = 3;
            avatarY = startY - 1;
            win();
            ter.renderFrame(intArrayToTETileArray(target));
        } else {
            swap(startX, startY, startX, startY - 1, target);
            avatarY = startY - 1;
            ter.renderFrame(intArrayToTETileArray(target));
        }
    }
    private TETile[][] intArrayToTETileArray(int[][] target) {
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                finalWorldFrame[x][y] = intToTileType(target[x][y]);
            }
        }
        return finalWorldFrame;
    }


    private void determineDirectionAndMove(KeyBoardInputSource inputSource,
                                           int[][] types,  ArrayList<Character> temp) {
        while (true) {
            char e = Character.toUpperCase(inputSource.getNextKey());
            inputTracker.add(e);
            while (e != ':') {
                if (e == 'W') {
                    moveUp(avatarX, avatarY, types);
                }
                if (e == 'A') {
                    moveLeft(avatarX, avatarY, types);
                }
                if (e == 'S') {
                    moveDown(avatarX, avatarY, types);
                }
                if (e == 'D') {
                    moveRight(avatarX, avatarY, types);
                }
                hud(intArrayToTETileArray(types));
                e = Character.toUpperCase(inputSource.getNextKey());
                temp.add(e);
            }
            hud(intArrayToTETileArray(types));
            e = Character.toUpperCase(inputSource.getNextKey());
            if (e == 'Q') {
                quitAndSave(inputTracker);
            } else {
                determineDirectionAndMove(inputSource, types, temp);
            }
        }
    }

    private void determineDirectionAndMove(Character e, String input, int[][] types,
                                           ArrayList<Character> temp,
            int start) {
        if (input.length() - 1 == start) {
            this.off = true;
            return;
        }
        while (e != ':') {
            temp.add(e);
            if (e == 'W') {
                moveUp(avatarX, avatarY, types);
            }
            if (e == 'A') {
                moveLeft(avatarX, avatarY, types);
            }
            if (e == 'S') {
                moveDown(avatarX, avatarY, types);
            }
            if (e == 'D') {
                moveRight(avatarX, avatarY, types);
            }
            start += 1;
            e = Character.toUpperCase(input.charAt(start));
        }
        temp.add(e);
        start += 1;
        if (start == input.length()) {
            this.off = true;
            return;
        }
        e = Character.toUpperCase(input.charAt(start));
        if (e == 'Q') {
            quitAndSave(temp);
        } else {
            determineDirectionAndMove(e, input, types, temp, start);
        }
    }
    private void quitAndSave(ArrayList<Character> input)  {
        saveFile = new File("output.txt");
        try {
            FileWriter fw = new FileWriter(saveFile);
            PrintWriter pw = new PrintWriter(fw);
            for (char a : input) {
                String temp = Character.toString(a);
                pw.print(temp);
            }
            this.off = true;
            pw.close();
            System.exit(0);
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        } catch (IOException f) {
            System.out.println(f);
        }
    }

    private String load()  {
        File temp = new File("output.txt");
        if (temp.exists()) {
            try {
                FileReader rd = new FileReader(temp);
                BufferedReader br = new BufferedReader(rd);
                String output = br.readLine();
                return output;
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
                return null;
            } catch (IOException f) {
                System.out.println(f);
                System.exit(0);
                return null;
            }
        } else {
            System.out.println("file not found");
            System.exit(0);
            return null;
        }
    }

    private int tileToInt(TETile temp) {
        if (temp == Tileset.NOTHING) {
            return 0;
        } else if (temp == Tileset.FLOOR) {
            return 1;
        } else if (temp == Tileset.AVATAR) {
            return 3;
        } else if (temp == Tileset.FLOWER) {
            return 4;
        } else {
            return 2;
        }
    }
    private void hud(TETile[][] finalWorldFrame) {
        int mX = (int) Math.floor(StdDraw.mouseX());
        int mY = (int) Math.floor(StdDraw.mouseY());
        while (!StdDraw.hasNextKeyTyped()) {
            if (checkIfMouseChanged(StdDraw.mouseX(), StdDraw.mouseY())) {
                mX = (int) Math.floor(StdDraw.mouseX());
                mY = (int) Math.floor(StdDraw.mouseY());
                ter.renderFrame(finalWorldFrame);
            }
            ter.renderFrame(finalWorldFrame);
            StdDraw.setPenColor(Color.white);
            StdDraw.textLeft(1, HEIGHT - 1, finalWorldFrame[mX][mY].description());
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            StdDraw.setPenColor(Color.white);
            StdDraw.textLeft(1, HEIGHT - 2, dateFormat.format(date));
            StdDraw.show();
        }
    }
    private void win() {
        StdDraw.clear(new Color(0, 0, 0));
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 40));
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT / 2 + 10, "Congratulations! You have collected the flower!");
        StdDraw.show();
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 14));
    }
    private boolean checkIfMouseChanged(double x, double y) {
        if (x != mouseX || y != mouseY) {
            mouseX = x;
            mouseY = y;
            return true;
        } else {
            return false;
        }
    }

    private void promptsForSeed() {
        StdDraw.clear(new Color(0, 0, 0));
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 15));
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(0.5 , 0.5, "Press the random seed following with the character s");
        StdDraw.show();
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 14));
    }
    public static void main(String[] args) {
        Engine a = new Engine();
        a.interactWithKeyboard();
    }
}
