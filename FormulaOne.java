import java.nio.Buffer;
import java.util.Vector;
import java.util.Random;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;

import javax.imageio.ImageIO;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.Polygon;
import java.awt.Color;

public class FormulaOne {
    public FormulaOne() { setup(); }
    // pg 179-180 all variables
    private static Boolean endgame;
    private static BufferedImage background;
    private static BufferedImage player;
    private static BufferedImage cockpit;
    private static BufferedImage track;
    private static BufferedImage perspectiveTrack;
    private static Vector<Vector<Vector<Integer>>> trackMatrix;

    private static int camerax;
    private static int cameray;

    private static int cockpitShift;

    private static Boolean upPressed;
    private static Boolean downPressed;
    private static Boolean leftPressed;
    private static Boolean rightPressed;

    private static ImageObject p1;
    private static double p1width;
    private static double p1height;
    private static double p1originalX;
    private static double p1originalY;
    private static double p1velocity;

    private static Long audiolifetime;
    private static Long lastAudioState;
    private static Clip clip;
    private static String backgroundState;

    private static int XOFFSET;
    private static int YOFFSET;
    private static int WINWIDTH;
    private static int WINHEIGHT;

    private static double pi;
    private static double quarterPi;
    private static double halfPi;
    private static double threequartersPi;
    private static double fivequartersPi;
    private static double threehalvesPi;
    private static double sevenquartersPi;
    private static double twoPi;

    private static JFrame appFrame;

    private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;

    // pg 160
    public static void setup() {
        appFrame = new JFrame("Formula 1");

        XOFFSET = 0;
        YOFFSET = 40;
        WINWIDTH = 257;
        WINHEIGHT = 257;

        pi = 3.14159265358979;
        quarterPi = 0.25 * pi;
        halfPi = 0.5 * pi;
        threequartersPi = 0.75 * pi;
        fivequartersPi = 1.25 * pi;
        threehalvesPi = 1.5 * pi;
        sevenquartersPi = 1.75 * pi;
        twoPi = 2.0 * pi;

        endgame = false;
        p1width = 228;
        p1height = 228;
        cockpitShift = 220;
        p1originalX = (double)XOFFSET + ((double)WINWIDTH / 2.0) - (p1width / 2.0) + 28;
        p1originalY = (double)YOFFSET + (double)cockpitShift;

        audiolifetime = 119000L; // 119 seconds or 1:59 minutes

        trackMatrix = new Vector<Vector<Vector<Integer>>>();

        try {
            background = ImageIO.read(new File("images/Tron_Skyline.png"));
            player = ImageIO.read(new File("images/Tron_Wheel.png"));
            cockpit = ImageIO.read(new File("images/Tron_Car.png"));
            track = ImageIO.read(new File("images/Tron_Ground.png"));
            perspectiveTrack = convertToARGB(ImageIO.read(new File("images/Tron_Ground.png")));
        } catch (IOException ioe) { }
    }

    // pg 161
    private static BufferedImage convertToARGB(BufferedImage input) {
        BufferedImage ret = new BufferedImage(input.getWidth(), input.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ret.createGraphics();
        g.drawImage(input, 0, 0, null);
        g.dispose();
        return ret;
    }

    // pg 161
    private static class Animate implements Runnable {
        public void run() {
            while (endgame == false) {
                backgroundDraw();
                trackDraw();
                playerDraw();

                try {
                    Thread.sleep(32);
                } catch (InterruptedException e) { }
            }
        }
    }

    private static class AudioLooper implements Runnable {
        public void run() {
            while (endgame == false) {
                Long currTime = Long.valueOf(System.currentTimeMillis());
                if (currTime - lastAudioState > audiolifetime) {
                    playAudio(backgroundState);
                }
            }
        }
    }
    
    private static void playAudio(String backgroundState) {
        try {
            clip.stop();
        } catch (Exception e) {
            // NOP
        }

         try {
             AudioInputStream ais = AudioSystem.getAudioInputStream(new File("audio/UproarbyMichaelBriguglio.wav").getAbsoluteFile());
             clip = AudioSystem.getClip();
             clip.open(ais);
             clip.start();
             lastAudioState = System.currentTimeMillis();
             audiolifetime = Long.valueOf(119000);
         } catch (Exception e) {
             // NOP
         }
    }

    // pg 161-162
    private static class PlayerMover implements Runnable {
        private double velocitystep;
        private double rotatestep;

        public PlayerMover() {
            velocitystep = 0.01;
            rotatestep = 0.02;
        }

        public void run() {
            while (endgame == false) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                }

                if (upPressed) {
                    p1velocity = p1velocity + velocitystep;
                }

                if (downPressed) {
                    p1velocity = p1velocity - velocitystep;
                }

                if (leftPressed) {
                    if (p1velocity < 0) {
                        p1.rotate(-rotatestep);
                    } else {
                        p1.rotate(rotatestep);
                    }
                }

                if (rightPressed) {
                    if (p1velocity < 0) {
                        p1.rotate(rotatestep);
                    } else {
                        p1.rotate(-rotatestep);
                    }
                }
            }
        }
    }

    // pg 163
    private static int constrainToCap(int position, int differential, int cap) {
        int ret = differential;
        while (position + ret < 0) {
            ret = ret + cap;
        }

        while (position + ret >= cap) {
            ret = ret - cap;
        }
        // ret = (position + ret) % cap;
        return ret;
    }

    // pg 163-164
    private static class CameraMover implements Runnable {
        public CameraMover() {}

        public void run() {
            while (endgame == false) {
                try {
                    Thread.sleep(10);
                } catch (Exception e) {

                }

                // TODO: why does the velocity not impact the movement until about +1.5?
                int sumx = (int)(-p1velocity * Math.cos(p1.getAngle() - pi / 2.0) + 0.5);
                int sumy = (int)(p1velocity * Math.sin(p1.getAngle() - pi / 2.0) + 0.5);

                camerax = camerax + constrainToCap(camerax, sumx, trackMatrix.elementAt(0).size());
                cameray = cameray + constrainToCap(cameray, sumy, trackMatrix.size());
            }
        }
    }

    // pg 164
    private static Vector<Vector<Vector<Integer>>> splitColors (BufferedImage input) {
        Vector<Vector<Vector<Integer>>> ret = new Vector<Vector<Vector<Integer>>>();

        for (int i = 0; i < input.getWidth(); i++) {
            Vector<Vector<Integer>> tempRow = new Vector<Vector<Integer>>();

            for (int j = 0; j < input.getHeight(); j++) {
                Vector<Integer> temp = new Vector<Integer>();
                int rgb = input.getRGB(i, j);
                int r = (rgb >> 16) & 0x000000FF;
                int g = (rgb >> 8) & 0x000000FF;
                int b = rgb & 0x000000FF;

                temp.add(r);
                temp.add(g);
                temp.add(b);
                tempRow.add(temp);
            }
            ret.add(tempRow);
        }
        return ret;
    }

    // pg 164
    private static void setupTrack() {
        trackMatrix = splitColors(track);
    }

    //pg 164
    private static AffineTransformOp rotateImageObject(ImageObject obj) {
        AffineTransform at = AffineTransform.getRotateInstance(-obj.getAngle(), obj.getWidth() / 2.0,
                obj.getHeight() / 2.0);
        AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return atop;
    }

    // pg 165
    private static AffineTransformOp spinImageObject(ImageObject obj) {
        AffineTransform at = AffineTransform.getRotateInstance(-obj.getInternalAngle(), obj.getWidth() / 2.0,
                obj.getHeight() / 2.0);
        AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return atop;
    }

    // pg 165
    private static void backgroundDraw() {
        Graphics g = appFrame.getGraphics();
        Graphics2D g2D = (Graphics2D) g;

        int xshift = XOFFSET + (int)((p1.getAngle() / twoPi) * (double) background.getWidth() + 0.5);
        g2D.drawImage(background, xshift, YOFFSET, null);
        g2D.drawImage(background, xshift - background.getWidth(), YOFFSET, null);
        g2D.drawImage(cockpit, XOFFSET, cockpitShift, null);
        g2D.drawImage(rotateImageObject(p1).filter(player, null), (int)(p1.getX() + 0.5), (int)(p1.getY() + 0.5), null);
    }

    // pg 165-166
    private static Vector<Vector<Vector<Integer>>> perspectiveFromRectangle(Vector<Vector<Vector<Integer>>> inputGrid, int base) {
        Vector<Vector<Vector<Integer>>> ret = new Vector<Vector<Vector<Integer>>>();

        // allocate space for ret
        for (int i = 0; i < inputGrid.size(); i++) {
            Vector<Vector<Integer>> tempRow = new Vector<Vector<Integer>>();

            for (int j = 0; j < inputGrid.elementAt(i).size(); j++) {
                Vector<Integer> tempRGB = new Vector<Integer>();
                tempRGB.add(0);
                tempRGB.add(0);
                tempRGB.add(0);

                tempRow.add(tempRGB);
            }
            ret.add(tempRow);
        }

        // collapse rows from inputGrid into ret
        for (int i = 0; i < inputGrid.size(); i++) {
            for (int j = 0; j < inputGrid.elementAt(i).size(); j++) {
                double xdim = (double)inputGrid.elementAt(i).size();
                double ydim = (double)inputGrid.size();
                double width = xdim - ((double)i / (ydim - 1.0)) * (xdim - (double) base);
                double stepsize = width / xdim;
                double offset = (xdim - width) / 2.0;
                int indexi = i;
//                int indexj = j;
                int indexj = (int) (0.5 + offset + (double)j * stepsize);
//                System.out.println("width: " + width + ", xdim: " + xdim + ", ydim: " + ydim);
//                System.out.println("i: " + i + ", j: " + j + ", indexi: " + indexi + ", indexj: " + indexj + ", offset: " + offset + ", stepsize: " + stepsize);
                ret.elementAt(i).elementAt(j).set(0, inputGrid.elementAt(indexi).elementAt(indexj).elementAt(0));
                ret.elementAt(i).elementAt(j).set(1, inputGrid.elementAt(indexi).elementAt(indexj).elementAt(1));
                ret.elementAt(i).elementAt(j).set(2, inputGrid.elementAt(indexi).elementAt(indexj).elementAt(2));
            }
        }
        return ret;
    }

    // pgs 166-168
    private static Vector<Vector<Vector<Integer>>> rotateImage(Vector<Vector<Vector<Integer>>> inputImg, double angle,
                                                               double xpos, double ypos, boolean repeatImg) {
        Vector<Vector<Vector<Integer>>> ret = new Vector<Vector<Vector<Integer>>>();

        for (int i = 0; i < inputImg.size(); i++) {
            Vector<Vector<Integer>> tempRow = new Vector<Vector<Integer>>();

            for (int j = 0; j < inputImg.elementAt(i).size(); j++) {
                Vector<Integer> tempPixel = new Vector<Integer>();
                for (int k = 0; k < inputImg.elementAt(i).elementAt(j).size(); k++) {
                    tempPixel.add(0);
                }
                tempRow.add(tempPixel);
            }
            ret.add(tempRow);
        }

        for (int i = 0; i < inputImg.size(); i++) {
            for (int j = 0; j < inputImg.elementAt(i).size(); j++) {
                int newj = (int)(0.5 + xpos + ((double)j - xpos) * Math.cos(angle) - ((double)i - ypos) *
                        Math.sin(angle));
                int newi = (int)(0.5 + ypos + ((double)j - ypos) * Math.sin(angle) + ((double)i - ypos) *
                        Math.cos(angle));
                if (repeatImg) {
                    while (newj >= ret.elementAt(0).size()) {
                        newj = newj - ret.elementAt(0).size();
                    }

                    while (newj < 0) {
                        newj = newj + ret.elementAt(0).size();
                    }

                    while (newi >= ret.size()) {
                        newi = newi - ret.size();
                    }

                    while (newi < 0) {
                        newi = newi + ret.size();
                    }
                }

                if (newj < ret.elementAt(0).size() && newj >= 0) {
                    if (newi < ret.size() && newi >= 0) {
                        ret.elementAt(newi).elementAt(newj).set(0, inputImg.elementAt(i).elementAt(j).elementAt(0));
                        ret.elementAt(newi).elementAt(newj).set(1, inputImg.elementAt(i).elementAt(j).elementAt(1));
                        ret.elementAt(newi).elementAt(newj).set(2, inputImg.elementAt(i).elementAt(j).elementAt(2));
                    }
                }
            }
        }
            return ret;
    }

    // pg 168
    private static Vector<Vector<Vector<Integer>>> duplicate3x3(Vector<Vector<Vector<Integer>>> inputImg) {
        Vector<Vector<Vector<Integer>>> ret = new Vector<Vector<Vector<Integer>>>();

        for (int i = 0; i < inputImg.size() * 3; i++) {
            Vector<Vector<Integer>> tempRow = new Vector<Vector<Integer>>();

            for (int j = 0; j < inputImg.elementAt(0).size() * 3; j++) {
                Vector<Integer> tempPixel = new Vector<Integer>();
                tempPixel.add(0);
                tempPixel.add(0);
                tempPixel.add(0);

                tempRow.add(tempPixel);
            }
            ret.addElement(tempRow);
        }

        for (int i = 0; i < ret.size(); i++) {
            for (int j = 0; j < ret.elementAt(i).size(); j++) {
                ret.elementAt(i).elementAt(j).set(0, inputImg.elementAt(i % inputImg.size()).
                        elementAt(j % inputImg.elementAt(0).size()).elementAt(0));
                ret.elementAt(i).elementAt(j).set(1, inputImg.elementAt(i % inputImg.size()).
                        elementAt(j % inputImg.elementAt(0).size()).elementAt(1));
                ret.elementAt(i).elementAt(j).set(2, inputImg.elementAt(i % inputImg.size()).
                        elementAt(j % inputImg.elementAt(0).size()).elementAt(2));
            }
        }
        return ret;
    }

    // pgs 168-170
    private static void trackDraw() {
        // use camera's position, p1's rotation, and trapezoid mapper

        int rectWidth = 34;// 500;
        int rectHeight = 34;//175;
        int base = 34;//150
        int xoffset = 0;
        int yoffset = 140;//232
        int scaledown = 5;

        Vector<Vector<Vector<Integer>>> cameraView = new Vector<Vector<Vector<Integer>>>();

        for (int i = 0; i < rectHeight; i++) {
            Vector<Vector<Integer>> tempRow = new Vector<Vector<Integer>>();
            for (int j = 0; j < rectWidth; j++) {
                Vector<Integer> tempRGB = new Vector<Integer>();

                int indexi = cameray - (rectHeight - i); // % trackMatrix.size();
                int indexj = camerax - (rectWidth - j + (int)(0.5 + ((double) rectWidth / 2.0))); // % trackMatrix.elementAt(0).size();

                while (indexi < 0) {
                    indexi = indexi + trackMatrix.size();
                }

                while (trackMatrix.size() <= indexi) {
                    indexi = indexi - trackMatrix.size();
                }

                while (indexj < 0) {
                    indexj = indexj + trackMatrix.elementAt(0).size();
                }

                while (trackMatrix.elementAt(0).size() < indexj) {
                    indexj = indexj - trackMatrix.elementAt(0).size();
                }

                tempRGB.add(trackMatrix.elementAt(indexi).elementAt(indexj).elementAt(0));
                tempRGB.add(trackMatrix.elementAt(indexi).elementAt(indexj).elementAt(1));
                tempRGB.add(trackMatrix.elementAt(indexi).elementAt(indexj).elementAt(2));

                tempRow.add(tempRGB);
            }
            cameraView.add(tempRow);
        }

        Vector<Vector<Vector<Integer>>> userview = perspectiveFromRectangle(cameraView, base);

        Graphics g = appFrame.getGraphics();
        Graphics2D g2D = (Graphics2D) g;

        for (int i = 0; i < rectHeight; i++) {
            for (int j = 0; j < rectWidth; j++) {
                int alpha = 255;
                int red = userview.elementAt(i).elementAt(j).elementAt(0);
                int green = userview.elementAt(i).elementAt(j).elementAt(1);
                int blue = userview.elementAt(i).elementAt(j).elementAt(2);

                while (red < 0) {
                    red += 256;
                }

                while (256 <= red) {
                    red -= 256;
                }

                while (green < 0) {
                    green += 256;
                }

                while (256 <= green) {
                    green -= 256;
                }

                while (blue < 0) {
                    blue += 256;
                }

                while (256 <= blue) {
                    blue -= 256;
                }

                Color myColor = new Color(red, green, blue);
                int rgb = myColor.getRGB();
                perspectiveTrack.setRGB(j, i, rgb);
            }
        }
        g2D.drawImage(perspectiveTrack, XOFFSET, YOFFSET + yoffset, null);
    }

    private static void playerDraw() {
        Graphics g = appFrame.getGraphics();
        Graphics2D g2D = (Graphics2D) g;
        g2D.drawImage(cockpit, XOFFSET + 28, cockpitShift, null);
        g2D.drawImage(rotateImageObject(p1).filter(player, null), (int)(p1.getX() + 0.5), (int)(p1.getY() + 0.5), null);
    }

    private static class KeyPressed extends AbstractAction {
        private String action;

        public KeyPressed() { action = ""; }
        public KeyPressed(String input) { action = input; }

        public void actionPerformed(ActionEvent e) {
            if (action.equals("UP")) {
                upPressed = true;
            }
            if (action.equals("DOWN")) {
                downPressed = true;
            }
            if (action.equals("LEFT")) {
                leftPressed = true;
            }
            if (action.equals("RIGHT")) {
                rightPressed = true;
            }
        }
    }

    private static class KeyReleased extends AbstractAction {
        private String action;

        public KeyReleased() { action = ""; }
        public KeyReleased(String input) { action = input; }

        public void actionPerformed(ActionEvent e) {
            if(action.equals("UP")) {
                upPressed = false;
            }
            if (action.equals("DOWN")) {
                downPressed = false;
            }
            if (action.equals("LEFT")) {
                leftPressed = false;
            }
            if (action.equals("RIGHT")) {
                rightPressed = false;
            }
        }
    }

    private static class QuitGame implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            endgame = true;
        }
    }

    private static class StartGame implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            endgame = true;
            upPressed = false;
            downPressed = false;
            leftPressed = false;
            rightPressed = false;
            p1 = new ImageObject(p1originalX, p1originalY, p1width, p1height, 0.0);
            p1velocity = 0.0;
            camerax = 0;
            cameray = 0;
            backgroundState = "startState";
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) { }
            setupTrack();
            lastAudioState = System.currentTimeMillis();
            playAudio(backgroundState);
            endgame = false;
            Thread t1 = new Thread(new Animate());
            Thread t2 = new Thread(new PlayerMover());
            Thread t3 = new Thread(new CameraMover());
            Thread t4 = new Thread(new AudioLooper());
            t1.start();
            t2.start();
            t3.start();
            t4.start();
        }
    }

    private static class ImageObject {
        // vars of ImageObject
        private double x;
        private double y;
        private double xwidth;
        private double yheight;
        private double angle; // in Radians
        private double internalangle; // in Radians
        private Vector<Double> coords;
        private Vector<Double> triangles;
        private double comX;
        private double comY;

        public ImageObject() {

        }

        public ImageObject(double xinput, double yinput, double xwidthinput, double yheightinput, double angleinput) {
            x = xinput;
            y = yinput;
            xwidth = xwidthinput;
            yheight = yheightinput;
            angle = angleinput;
            internalangle = 0.0;
            coords = new Vector<Double>();
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getWidth() { return xwidth; }
        public double getHeight() { return yheight; }
        public double getAngle() { return angle; }
        public double getInternalAngle() { return internalangle; }
        public void setAngle(double angleinput) { angle = angleinput; }
        public void setInternalAngle(double internalangleinput) { internalangle = internalangleinput; }
        public Vector<Double> getCoords() { return coords; }
        public void setCoords(Vector<Double> coordsinput) {
            coords = coordsinput;
            generateTriangles();
            // printTriangles();
        }

        public void generateTriangles() {
            triangles = new Vector<Double>();
            // format: (0, 1), (2, 3), (4, 5) is the (x, y) coords of a triangle

            // get center point of all coordinates
            comX = getComX();
            comY = getComY();

            for (int i = 0; i < coords.size(); i = i + 2) {
                triangles.addElement(coords.elementAt(i));
                triangles.addElement(coords.elementAt(i + 1));

                triangles.addElement(coords.elementAt((i+2) % coords.size()));
                triangles.addElement(coords.elementAt((i+3) % coords.size()));

                triangles.addElement(comX);
                triangles.addElement(comY);
            }
        }

        public void printTriangles() {
            for (int i = 0; i < triangles.size(); i = i + 6) {
                System.out.println("p0x: " + triangles.elementAt(i) + ", p0y: " + triangles.elementAt(i+1));
                System.out.println("p1x: " + triangles.elementAt(i+2) + ", p1y: " + triangles.elementAt(i+3)
                        + triangles.elementAt(i+3));
                System.out.println("p2x: " + triangles.elementAt(i+4) + ", p2y: " + triangles.elementAt(i+5));
            }
        }

        public double getComX() {
            double ret = 0;
            if (coords.size() > 0) {
                for (int i = 0; i < coords.size(); i = i + 2) {
                    ret = ret + coords.elementAt(i);
                }
                ret = ret / (coords.size() / 2.0);
            }
            return ret;
        }

        public double getComY() {
            double ret = 0;
            if (coords.size() > 0) {
                for (int i = 1; i < coords.size(); i = i + 2) {
                    ret = ret + coords.elementAt(i);
                }
                ret = ret / (coords.size() / 2.0);
            }
            return ret;
        }

        public void move(double xinput, double yinput) {
            x = x + xinput;
            y = y + yinput;
        }

        public void moveto(double xinput, double yinput) {
            x = xinput;
            y = yinput;
        }

        public void screenWrap(double leftEdge, double rightEdge, double topEdge, double bottomEdge) {
            if (x > rightEdge) {
                moveto(leftEdge, getY());
            }
            if (x < leftEdge) {
                moveto(rightEdge, getY());
            }
            if (y > bottomEdge) {
                moveto(getX(), topEdge);
            }
            if (y < topEdge) {
                moveto(getX(), bottomEdge);
            }
        }

        public void rotate(double angleinput) {
            angle = angle + angleinput;
            while (angle > twoPi) {
                angle = angle - twoPi;
            }

            while (angle < 0) {
                angle = angle + twoPi;
            }
        }

        public void spin(double internalangleinput) {
            internalangle = internalangle + internalangleinput;
            while (internalangle > twoPi) {
                internalangle = internalangle - twoPi;
            }

            while (internalangle < 0) {
                internalangle = internalangle + twoPi;
            }
        }
    }

    private static void bindKey(JPanel myPanel, String input) {
        myPanel.getInputMap(IFW).put(KeyStroke.getKeyStroke("pressed " + input), input + " pressed");
        myPanel.getActionMap().put(input + " pressed", new KeyPressed(input));

        myPanel.getInputMap(IFW).put(KeyStroke.getKeyStroke("released " + input), input + " released");
        myPanel.getActionMap().put(input + " released", new KeyReleased(input));
    }

    public static void main(String[] args) {
        setup();
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.setSize(WINWIDTH + 1, WINHEIGHT + 85);

        JPanel myPanel = new JPanel();

        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(new StartGame());
        myPanel.add(newGameButton);

        JButton quitButton = new JButton("Quit Game");
        newGameButton.addActionListener(new QuitGame());
        myPanel.add(quitButton);

        bindKey(myPanel, "UP");
        bindKey(myPanel, "DOWN");
        bindKey(myPanel, "LEFT");
        bindKey(myPanel, "RIGHT");
        bindKey(myPanel, "F");

        appFrame.getContentPane().add(myPanel, "South");
        appFrame.setVisible(true);
    }
}