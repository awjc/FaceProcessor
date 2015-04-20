package cv;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class TransformCalculator {
	private static final String IN_FILE = "C:/Users/Adam/Dropbox/Faces/processed/eyeLocations.txt";
	private static final String OUT_FILE = "C:/Users/Adam/Dropbox/Faces/processed/eyeTransforms.txt";
	private static List<Face> faces = new ArrayList<>();
	private static PrintWriter out;
	
	public static void main(String[] args) {
		Scanner in = null;
		try {
			in = new Scanner(new File(IN_FILE));
			out = new PrintWriter(new File(OUT_FILE));
			
			while (in.hasNextLine()) {
				String line = in.nextLine();
				processLine(line);
			}
			
			if (faces.size() > 0) {
				for (Face f : faces) {
					processEye(f.fileName, f.lx, f.ly, f.rx, f.ry);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				in.close();
			}
			if(out != null) {
				out.close();
			}
		}
	}
	
	/**
	 * Process a line
	 * 
	 * Expected format: <faceNum>,<faceFile>,<leftEyeX>,<leftEyeY>,<rightEyeX>,<rightEyeY> 
	 * @param line
	 */
	private static void processLine(String line) {
		Scanner lineScanner = new Scanner(line);
		lineScanner.useDelimiter("(,| )+");
		try {
			@SuppressWarnings("unused")
			int faceNum = lineScanner.nextInt();
			String fileName = lineScanner.next();
			int lx = lineScanner.nextInt();
			int ly = lineScanner.nextInt();
			int rx = lineScanner.nextInt();
			int ry = lineScanner.nextInt();
			
			if (lx >= 0 && ly >= 0 && rx >= 0 && ry >= 0) {
				faces.add(new Face(fileName, lx, ly, rx, ry));
			}
		} catch (NoSuchElementException e) {
			System.err.println("Skipping line: " + line);
		} finally {
			if (lineScanner != null) {
				lineScanner.close();
			}
		}
	}
	
	private static void processEye(String fileName, int lx, int ly, int rx, int ry) {
		Face upperLeft = findUpperLeftBounds();
		int baseX = upperLeft.lx;
		int baseY = upperLeft.ly;
		int offsetX = (lx - baseX);
		int offsetY = (ly - baseY);
		
		double dy = (ly - ry);
		double dx = (rx - lx);
		double theta = Math.toDegrees(Math.atan(dy / dx));
		
		double maxEyeSeparation = findMaxEyeSeparation();
		double scaleFactor = 1.0 / ((rx - lx) / maxEyeSeparation);
		
		String msg = String.format("%s,%d,%d,%.2f,%.2f", fileName, offsetX, offsetY, theta, scaleFactor);
		out.println(msg);
		System.out.println(msg);
	}
	
	private static Face findUpperLeftBounds() {
		Face upperLeft = new Face("", faces.get(0).lx, faces.get(0).ly, faces.get(0).rx, faces.get(0).ry);
		for(Face f : faces) {
			if (f.lx < upperLeft.lx) {
				upperLeft.lx = f.lx;
			} 
			if (f.ly < upperLeft.ly) {
				upperLeft.ly = f.ly;
			}
		}
		
		return upperLeft;
	}
	
	private static double findMaxEyeSeparation() {
		double maxEyeSep = faces.get(0).rx - faces.get(0).lx;
		for(Face f : faces) {
			double eyeSep = f.rx - f.lx;
			if (eyeSep > maxEyeSep) {
				maxEyeSep = eyeSep;
			}
		}
		
		return maxEyeSep;
	}
	
	private static class Face {
		private String fileName;
		private int lx;
		private int ly;
		private int rx;
		private int ry;
		
		public Face(String fileName, int lx, int ly, int rx, int ry) {
			this.fileName = fileName;
			this.lx = lx;
			this.ly = ly;
			this.rx = rx;
			this.ry = ry;
		}
		
		@Override
		public String toString() {
			return "(" + lx + "," + ly + "," + rx + "," + ry + ")";
		}
	}
}
