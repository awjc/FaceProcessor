package cv;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvScalar;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvEqualizeHist;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.googlecode.javacpp.Pointer;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

public class FaceProcessor {
	private static final String CASCADE_PATH = "C:\\opencv\\data\\haarcascades\\";
    private static final CvMemStorage STORAGE = CvMemStorage.create();
    private static final String faceDir = "C:/Users/Adam/Dropbox/Faces/raw/";
    
//	private static int frameCount;
	private static boolean showFrames = false;
	
	private static final String OUT_FILE = "C:/Users/Adam/Dropbox/Faces/processed/eyeLocations.txt";
	private static PrintWriter out;
	
	public static void main(String[] args) throws IOException {
//		final CanvasFrame frame = new CanvasFrame("original", CanvasFrame.getDefaultGamma());///grabber.getGamma());
//		frame.getCanvas().addMouseListener(new MouseAdapter() {
//			@Override
//			public void mousePressed(MouseEvent e) {
//				if(e.getButton() == MouseEvent.BUTTON2) {
//					dispose(frame);
//				}
//			}
//		});
		out = new PrintWriter(new File(OUT_FILE));
		
		File f = new File(faceDir);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Image", "jpg", "jpeg", "png");
		if(!f.exists() || !f.isDirectory()) {
			System.err.println("Error! Not a valid face directory: " + faceDir);
		}
		
		String[] files = f.list();
//		frameCount = files.length;
		List<IplImage> images = new ArrayList<>();
		
		int k = 0;
		for (String faceFile : files) {
			File faceFileFile = new File(faceFile);
			if(!filter.accept(faceFileFile)) {
				//System.out.println("SKIPPING: " + faceDir + faceFile);
				continue;
			}
			
			k++;
			//System.out.println("Processing: " + faceDir + faceFile);
			IplImage img = cvLoadImage(faceDir + faceFile);
			//System.out.println("Image: " + img);
			IplImage result = cvCreateImage(cvGetSize(img), img.depth(), img.nChannels());
			cvCopy(img, result);
			
			IplImage imgGray = cvCreateImage(cvGetSize(img), IPL_DEPTH_8U, 1);
			cvCvtColor(img, imgGray, CV_BGR2GRAY);
			cvEqualizeHist(imgGray, imgGray);
			
			boolean validImg = processImg(imgGray, result, true, faceDir + faceFile);
			
//			if(validImg) {
//				processImg(result, result, false);
//				images.add(result);
//			
//				if(showFrames) {
//					final CanvasFrame frame = new CanvasFrame("Image " + k, CanvasFrame.getDefaultGamma());///grabber.getGamma());
//					frame.getCanvas().addMouseListener(new MouseAdapter() {
//						@Override
//						public void mousePressed(MouseEvent e) {
//							if(e.getButton() == MouseEvent.BUTTON2) {
//								dispose(frame);
//							}
//						}
//					});
//					frame.showImage(result);
//					frame.getCanvas().setSize(640, 480);
//					frame.pack();
//				}
//			}
//			
			cvReleaseImage(img);
			//opencv_core.cvFree(result);
			cvReleaseImage(imgGray);
			System.gc();
		}
		
//		IplImage result = images.get(0);
//		IplImage result2 = cvCreateImage(cvSize(800, 600), images.get(0).depth(), images.get(0).nChannels());
//		cvResize(result, result2);
//		cvSaveImage("C:/Users/Adam/Dropbox/Faces/processed/" + String.format("%04d", 0) + "_" +
//				files[0].substring(0, files[0].lastIndexOf('.')) + "-processed.jpg", result2);
	/*
		
		int j = 1;
		for(int i=1; i < images.size(); i++) {
			int dx = eyes.get(0).x - eyes.get(2*i).x;
			int ddx = eyes.get(1).x - eyes.get(2*i + 1).x - dx;
			int dy = eyes.get(0).y - eyes.get(2*i).y;
			System.out.println(dx + "(" + ddx + "), " + dy);
			if (dx < 0 && dy < 0 && (-dx - ddx >= 0)) {
				BufferedImage bi = images.get(i).getBufferedImage().getSubimage(-dx - ddx, -dy, 1, 1);
				IplImage newResult = cvCreateImage(cvGetSize(images.get(i)), images.get(i).depth(), images.get(i).nChannels());
				newResult.copyFrom(bi, 1.0, false, new Rectangle(0, 0, 0, 0));
				IplImage newResult2 = cvCreateImage(cvSize(800, 600), images.get(i).depth(), images.get(i).nChannels());
				cvResize(newResult, newResult2);
//				cvSaveImage("C:/Users/Adam/Dropbox/Faces/processed/" + files[i] + "-processed.jpg", newResult);
				cvSaveImage("C:/Users/Adam/Dropbox/Faces/processed/" + String.format("%04d", i) + "_" +  
						files[i].substring(0, files[i].lastIndexOf('.')) + "-processed.jpg", newResult2);
		
				cvAddWeighted(result, (1.0 - 1.0/(j + 1)), newResult, (1.0/(j + 1)), 1, result);
				j++;
				cvReleaseImage(newResult);
				cvReleaseImage(newResult2);
			}
		}*/
		
//		IplImage imgGray = cvCreateImage(cvGetSize(result), IPL_DEPTH_8U, 1);
		//cvCvtColor(result, imgGray, CV_BGR2GRAY);
		//cvEqualizeHist(imgGray, imgGray);
		
//		showFrames = true;
//		if(showFrames) {
//			final CanvasFrame frame = new CanvasFrame("processed", CanvasFrame.getDefaultGamma());///grabber.getGamma());
//			frame.getCanvas().addMouseListener(new MouseAdapter() {
//				@Override
//				public void mousePressed(MouseEvent e) {
//					if(e.getButton() == MouseEvent.BUTTON2) {
//						dispose(frame);
//					}
//				}
//			});
//			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			
//			frame.showImage(result);
//			frame.getCanvas().setSize(800, 600);
//			frame.pack();
//		}
		
		out.close();
	}
	
	private static List<Eye> eyes = new ArrayList<>();
	
	private static int nEyePairs = 0;
	
	private static boolean processImg(IplImage img, IplImage result, boolean rotate, String filePath) {
		String cascadeToUse = "haarcascade_mcs_lefteye.xml";
		CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_PATH + cascadeToUse));
		List<Eye> mEyes = new ArrayList<>();
		CvSeq objects = cvHaarDetectObjects(img, cascade, STORAGE, 2, 5, 10, new CvSize(200, 100), new CvSize(350, 250));
		int found = objects.total();
		boolean ret = true;
		Eye leftEye = null;
		Eye rightEye = null;
		if(found != 2) {
			//System.err.println("Error: File has " + found + " eyes!");
			Eye emptyEye = new Eye(-1, -1, -1, -1);
			leftEye = emptyEye;
			rightEye = emptyEye;
			ret = false;
		}
		for (int i=0; i < found ; i++) {
			Pointer p = cvGetSeqElem(objects, i);
			CvRect r = new CvRect(p);
			cvRectangle(img, cvPoint(r.x(), r.y()), cvPoint(r.x() + r.width(), r.y() + r.height()), cvScalar(1, 0, 100, 1), 5, CV_AA, 0);
			if (rotate) {
				Eye e = new Eye(r.x(), r.y(), r.width(), r.height());
				mEyes.add(e);
				//System.out.println("Eye found at : [" + e.x + ", " + e.y + ", " + e.width + ", " + e.height + "]");
			}
		}
		
		if (!rotate) return true;
		
		Collections.sort(mEyes, new Comparator<Eye>() {
			@Override
			public int compare(Eye e1, Eye e2) {
				return e1.x - e2.x;
			}
		});
		
		if (mEyes.size() == 2) {
			leftEye = mEyes.get(0);
			rightEye = mEyes.get(1);
		}
		
		++nEyePairs;
		out.println(String.format("%04d,%s,%s,%s", nEyePairs, filePath, leftEye, rightEye));
		System.out.println(String.format("%04d,%s,%s,%s", nEyePairs, filePath, leftEye, rightEye));

			/*double dy = (leftEye.y - rightEye.y);
			double dx = (rightEye.x - leftEye.x);
			
			double theta = Math.toDegrees(Math.atan(dy / dx));
			System.out.println("Theta: " + theta);
			
			CvMat matrix = cvCreateMat(2,3,CV_32FC1);
			cv2DRotationMatrix(cvPoint2D32f(leftEye.x, leftEye.y), -theta, 1, matrix);
			cvWarpAffine(result, result, matrix);
			cvReleaseMat(matrix);*/
		
		//eyes.addAll(mEyes);
		return ret;
	}
	
	private static void dispose(CanvasFrame f) {
		f.dispose();
//		if (--frameCount == 0) {
			System.exit(0);
//		}
	}
	
	private static class Eye extends Rectangle {
		private static final long serialVersionUID = -8443109811578581692L;

		public Eye(int x, int y, int w, int h) {
			super(x, y, w, h);
		}
		
		@Override
		public String toString() {
			int cx = x + width/2;
			int cy = y + height/2;
			if(cx >= 0) {
				return String.format("%04d,%04d", cx ,cy);
			} else {
				return String.format("%4d,%-4d", cx, cy);
			}
		}
	}
}
