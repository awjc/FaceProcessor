package cv;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.googlecode.javacpp.Pointer;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

public class EyeFinder {
	private static final String CASCADE_PATH = "C:\\opencv\\data\\haarcascades\\";
    private static final CvMemStorage STORAGE = CvMemStorage.create();
    
    private static final String fileName = "C:\\Users\\Adam\\Dropbox\\Faces\\raw\\2013-10-12.jpg";

	private static int frameCount = 3;
	
	public static void main(String[] args) {
		final CanvasFrame frame = new CanvasFrame("original", CanvasFrame.getDefaultGamma());///grabber.getGamma());
		frame.getCanvas().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON2) {
					dispose(frame);
				}
			}
		});
		IplImage img = cvLoadImage(fileName);
		System.out.println("Image: " + img);
		IplImage result = cvCreateImage(cvGetSize(img), img.depth(), img.nChannels());
		cvCopy(img, result);
		
		frame.showImage(img);
		frame.setSize(640, 480);
		
		final CanvasFrame frame2 = new CanvasFrame("processed", CanvasFrame.getDefaultGamma());///grabber.getGamma());
		frame2.getCanvas().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON2) {
					dispose(frame2);
				}
			}
		});
		
		IplImage imgGray = cvCreateImage(cvGetSize(img), IPL_DEPTH_8U, 1);
		cvCvtColor(img, imgGray, CV_BGR2GRAY);
		cvEqualizeHist(imgGray, imgGray);
		
		processImg(imgGray, result);
		
		processImg(result, result);
		
		frame2.showImage(imgGray);
		frame2.setSize(640, 480);
		
		final CanvasFrame frame3 = new CanvasFrame("result", CanvasFrame.getDefaultGamma());///grabber.getGamma());
		frame3.getCanvas().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON2) {
					dispose(frame3);
				}
			}
		});
		
		frame3.showImage(result);
		frame3.setSize(640, 480);
	}
	
	private static void processImg(IplImage img, IplImage result) {
		String cascadeToUse = "haarcascade_mcs_lefteye.xml";
		CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_PATH + cascadeToUse));
		
		List<Eye> eyes = new ArrayList<>();
		
		CvSeq objects = cvHaarDetectObjects(img, cascade, STORAGE, 2, 5, 10, new CvSize(200, 100), new CvSize(350, 250));
		int found = objects.total();
		for (int i=0; i < found ; i++) {
			Pointer p = cvGetSeqElem(objects, i);
			CvRect r = new CvRect(p);
			cvRectangle(img, cvPoint(r.x(), r.y()), cvPoint(r.x() + r.width(), r.y() + r.height()), cvScalar(1, 0, 100, 1), 5, 8, 0);
			eyes.add(new Eye(r.x(), r.y(), r.width(), r.height()));
		}
		
		Collections.sort(eyes, new Comparator<Eye>() {
			@Override
			public int compare(Eye e1, Eye e2) {
				return e1.x - e2.x;
			}
		});
		
		for (Eye e : eyes) {
			System.out.println("Eye found at : [" + e.x + ", " + e.y + ", " + e.width + ", " + e.height + "]");
		}
		
		if (eyes.size() == 2) {
			Eye leftEye = eyes.get(0);
			Eye rightEye = eyes.get(1);
			double dy = (leftEye.y - rightEye.y);
			double dx = (rightEye.x - leftEye.x);
			
			double theta = Math.toDegrees(Math.atan(dy / dx));
			System.out.println(theta);
			
			CvMat matrix = cvCreateMat(2,3,CV_32FC1);
			cv2DRotationMatrix(cvPoint2D32f(leftEye.x, leftEye.y), -theta, 1, matrix);
			cvWarpAffine(result, result, matrix);
		}
	}
	
	private static void dispose(CanvasFrame f) {
		f.dispose();
		if (--frameCount == 0) {
			System.exit(0);
		}
	}
	
	private static class Eye extends Rectangle {
		private static final long serialVersionUID = -8443109811578581692L;

		public Eye(int x, int y, int w, int h) {
			super(x, y, w, h);
		}
	}
}
