
package orbisoftware.pdfscribe;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PdfScribe {

	private ClippingSelector clippingSelector = new ClippingSelector();
	private GeneratePDF generatePDF = new GeneratePDF();
	private String myOS = System.getProperty("os.name").toLowerCase();

	private String fileName = "settings.xml";
	private HashMap<String, String> xmlMap = new HashMap<>();

	private int imageCount = 0;

	public static void main(String[] args) {

		PdfScribe pdfScribe = new PdfScribe();
		pdfScribe.loadXML();

		pdfScribe.generatePDF.setPageSize(pdfScribe.xmlMap.get("PdfPageSize"));
		pdfScribe.generatePDF.borderPercent = Float.parseFloat(pdfScribe.xmlMap.get("BorderPercent"));

		pdfScribe.setupDisplay();
	}

	private void loadXML() {

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(fileName);
			Element rootElem = doc.getDocumentElement();

			if (rootElem != null) {
				parseElements(rootElem);
			}
		} catch (Exception e) {

			System.out.println("Exception in loadXML(): " + e.toString());
		}
	}

	private void parseElements(Element root) {

		String name = "";

		if (root != null) {

			NodeList nl = root.getChildNodes();

			if (nl != null) {

				for (int i = 0; i < nl.getLength(); i++) {
					Node node = nl.item(i);

					if (node.getNodeName().equalsIgnoreCase("setting")) {

						NodeList childNodes = node.getChildNodes();

						for (int j = 0; j < childNodes.getLength(); j++) {

							Node child = childNodes.item(j);

							if (child.getNodeName().equalsIgnoreCase("name"))
								name = child.getTextContent();
							else if (child.getNodeName().equalsIgnoreCase("value"))
								xmlMap.put(name, child.getTextContent());
						}
					}
				}
			}
		}
	}

	public boolean isWindows() {

		return (myOS.indexOf("win") >= 0);
	}

	public boolean isUnix() {

		return (myOS.indexOf("nix") >= 0 || myOS.indexOf("nux") >= 0 || myOS.indexOf("aix") > 0);
	}

	public void setupDisplay() {

		Display display = new Display();
		Shell shell = new Shell(display, (SWT.ON_TOP | SWT.RESIZE | SWT.CLOSE | SWT.TITLE));
		RowLayout layout = new RowLayout();

		if (isWindows())
			shell.setSize(318, 70);
		else if (isUnix())
			shell.setSize(308, 37);

		shell.setText("Pdf Scribe");
		shell.setLayout(layout);
		shell.open();

		// ***** Bounds Button *****
		final Button bounds = new Button(shell, SWT.PUSH);
		bounds.setText("Bounds");
		bounds.setBounds(0, 0, 100, 30);
		Listener boundsListener = new Listener() {
			public void handleEvent(Event event) {
				clippingSelector.showClippingSelector();
			}
		};
		bounds.addListener(SWT.Selection, boundsListener);

		// ***** Snapshot Button *****
		final Button snapShot = new Button(shell, SWT.PUSH);
		snapShot.setText("SnapShot");
		snapShot.setBounds(100, 0, 100, 30);
		Listener snapShotListener = new Listener() {
			public void handleEvent(Event event) {
				if (clippingSelector.clipRect == null)
					return;
				// take a snapshot and save the image off for processing
				final Image image = new Image(display, clippingSelector.clipRect);
				final GC gc = new GC(display);
				gc.copyArea(image, clippingSelector.clipRect.x, clippingSelector.clipRect.y);
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
		savePDF.setBounds(200, 0, 100, 30);

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
