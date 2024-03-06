//import com.sun.source.util.Plugin;
//import ij.*;
//import ij.process.*;
//import ij.gui.*;
//import ij.plugin.*;
//import ij.plugin.frame.*;
//
//import java.util.Vector;
//import java.awt.Color;
//
//public class ImageTransformation {
//    public class Transforms_CSC313 implements Plugin {
//        private static int decFromHex(int r, int g, int b) {
//            int ret = 0;
//            ret = (r << 16) + (g << 8) + b;
//            return ret;
//        }
//
//        private Vector<Vector<Vector<Integer>>> splitColors(ImagePlus img) {
//            Vector<Vector<Vector<Integer>>> colors = new Vector<Vector<Vector<Integer>>>();
//            ColorProcessor cp = new ColorProcessor(img.getBufferedImage());
//
//            Color myColor = new Color(0, 0, 0);
//            for (int i = 0; i < img.getHeight(); i++) {
//                Vector<Vector<Integer>> tempRow = new Vector<Vector<Integer>>();
//                for (int j = 0; j < img.getWidth(); j++) {
//                    myColor = cp.getColor(j, i);
//                    Vector<Integer> tempColor = new Vector<Integer>();
//                    tempColor.addElement(myColor.getRed());
//                    tempColor.addElement((myColor.getGreen()));
//                    tempColor.addElement(myColor.getBlue());
//                    tempRow.addElement(tempColor);
//                }
//                colors.addElement(tempRow);
//            }
//            return colors;
//        }
//
//        private static void writeToImage(Vector<Vector<Vector<Integer>>> inputImage, ImagePlus img, ImageProcessor imp) {
//            for (int i = 0; i < inputImage.size(); i++) {
//                for (int j = 0; j < inputImage.elementAt(i).size(); j++) {
//                    int color = decFromHex(inputImage.elementAt(i).elementAt(j).elementAt(0),
//                            inputImage.elementAt(i).elementAt(j).elementAt(1),
//                            inputImage.elementAt(i).elementAt(j).elementAt(2));
//                    img.putPixel(j, i, color);
//                }
//            }
//            img.updateAndDraw();
//        }
//
//        private static Vector<Vector<Vector<Integer>>> transform(Vector<Vector<Vector<Integer>>> inputImage,
//                                                                 String transformName, int Tx, int Ty, int Sx, int Sy,
//                                                                 double angle) {
//            Vector<Vector<Vector<Integer>>> ret = new Vector<Vector<Vector<Integer>>>();
//            for (int i = 0; i < inputImage.size(); i++) {
//                Vector<Vector<Integer>> tempRow = new Vector<Vector<Integer>>();
//                for (int j = 0; j < inputImage.elementAt(0).size(); j++) {
//                    Vector<Integer> tempRGB = new Vector<Integer>();
//                    tempRGB.addElement(0);
//                    tempRGB.addElement(0);
//                    tempRGB.addElement(0);
//
//                    tempRGB.addElement(tempRGB);
//                }
//                ret.addElement(tempRow);
//            }
//
//            for (int i = 0; i < inputImage.size(); i++) {
//                for (int j = 0; j < inputImage.elementAt(i).size(); j++) {
//                    int newI = 0;
//                    int newJ = 0;
//                    if (transformName.substring(0, 5).equals("Trans")) {
//                        newI = i + Ty;
//                        newJ = j + Tx;
//                    }
//
//                    if (transformName.substring(0, 5).equals("Rotat")) {
//                        newI = (int)(0.5 + (double)j * Math.sin(angle) + (double)i * Math.cos(angle));
//                        newJ = (int)(0.5 + (double)j * Math.cos(angle) - (double)i * Math.sin(angle));
//                    }
//
//                    if (transformName.substring(0, 5).equals("Compo")) {
////                        System.out.println("Compo");
//                        newI = (int)(0.5 + ((double)j - (double)Tx) * Math.sin(angle) + ((double)i - (double)Ty)*
//                                Math.cos(angle) + (double)Ty);
//                        newJ = (int)(0.5 + ((double)j - (double)Tx) * Math.cos(angle) + ((double)i - (double)Ty) *
//                                -1.0 * Math.sin(angle) + (double)Tx);
//                    }
//
//                    if (transformName.substring(0,5).equals("Scale")) {
//                        newI = (int)(0.5 + (double)Sy * (double)i);
//                        newJ = (int)(0.5 + (double)Sx * (double)j);
//                    }
//
//                    if (transformName.substring(0, 5).equals("SheaX")) {
//                        newI = i;
//                        newJ = j + Sx * i;
//                    }
//
//                    if (newI >= 0 && newI < inputImage.size()) {
//                        if (newJ >= 0 && newJ < inputImage.size()) {
//                            ret.elementAt(i).elementAt(j).set(0, inputImage.elementAt(newI).elementAt(newJ).elementAt(0));
//                            ret.elementAt(i).elementAt(j).set(1, inputImage.elementAt(newI).elementAt(newJ).elementAt(1));
//                            ret.elementAt(i).elementAt(j).set(2, inputImage.elementAt(newI).elementAt(newJ).elementAt(2));
//                        }
//                    }
//                }
//            }
//            return ret;
//        }
//
//        public void run(String arg) {
//            ImagePlus img = IJ.getImage();
//            ImageProcessor imp = img.getProcessor();
//
//            Vector<Vector<Vector<Integer>>> colors = splitColors(img);
//
//            String transformName = "SheaX";
//            int Tx = colors.elementAt(0).size() / 2; // 125;
//            int Ty = colors.size() / 2; // 230
//            int Sx = 10;
//            int Sy = 150;
//            double angle = 1.2;
//            for (int i = 0; i < 1000; i++) {
//                Vector<Vector<Vector<Integer>>> transformedImage = transform(colors, transformName, Tx, Ty,
//                        (int)(0.5 + (double)Sx * (double)i / 1000.0), (int)(0.5 + (double)Sy * (double)i / 1000.0),
//                        angle * (double)i / 1000.0);
//                writeToImage(transformedImage, img, imp);
//
//                try {
//                    Thread.sleep(32);
//                } catch (Exception e) {
//
//                }
//            }
//        }
//    }
//}
