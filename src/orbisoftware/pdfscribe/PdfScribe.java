
package orbisoftware.pdfscribe;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import java.awt.image.BufferedImage;

public class PdfScribe {

	private ClippingSelector clippingSelector = new ClippingSelector();
	private GeneratePDF generatePDF = new GeneratePDF();
	private String myOS = System.getProperty("os.name").toLowerCase();
	
	int imageCount = 0;

	public static void main(String[] args) {
		
		PdfScribe pdfScribe = new PdfScribe();
		pdfScribe.setupDisplay();
	}
	
	public boolean isWindows() {

		return (myOS.indexOf("win") >= 0);
	}

	public boolean isUnix() {

		return (myOS.indexOf("nix") >= 0 || myOS.indexOf("nux") >= 0 || myOS.indexOf("aix") > 0 );
	}
	
	public void setupDisplay() {
		
		Display display = new Display();
		Shell shell = new Shell(display);
		
		if (isWindows())
			shell.setSize(318, 65);
		else if (isUnix())
			shell.setSize(300, 55);
		
		shell.setText("Pdf Scribe");
		shell.open();

		// ***** Bounds Button *****
		final Button bounds = new Button(shell, SWT.PUSH);
		bounds.setText("Bounds");
		bounds.setBounds(0, 0, 100, 25);
		Listener boundsListener = new Listener() {
			public void handleEvent(Event event) {
				clippingSelector.showClippingSelector();
			}
		};
		bounds.addListener(SWT.Selection, boundsListener);
		
		// ***** Snapshot Button *****
		final Button snapShot = new Button(shell, SWT.PUSH);
		snapShot.setText("SnapShot");
		snapShot.setBounds(100, 0, 100, 25);
		Listener snapShotListener = new Listener() {
			public void handleEvent(Event event) {
				if (clippingSelector.clipRect == null)
					return;
				// take a snapshot and save the image off for processing
				final Image image = new Image(display, clippingSelector.clipRect);
				final GC gc = new GC(display);
				gc.copyArea(image, clippingSelector.clipRect.x,
						clippingSelector.clipRect.y);
				gc.dispose();
				
				BufferedImage saveImage = ImageCoversion.convertToAWT(image.getImageData());
				generatePDF.imageList.add(saveImage);
				imageCount++;
				
				System.out.println("SnapShot #" + Integer.toString(imageCount));
			}
		};
		snapShot.addListener(SWT.Selection, snapShotListener);
		
		// ***** Save PDF Button *****
		final Button savePDF = new Button(shell, SWT.PUSH);
		savePDF.setText("Save PDF");
		savePDF.setBounds(200, 0, 100, 25);

		Listener savePDFListener = new Listener() {
			public void handleEvent(Event event) {
				if (generatePDF.imageList.size() > 0) {
					generatePDF.generatePDF(shell);
					System.exit(0);
				}
			}
		};
		savePDF.addListener(SWT.Selection, savePDFListener);

		// Sleep until disposed
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		display.dispose();
	}
}
