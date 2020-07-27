package orbisoftware.pdfscribe;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class GeneratePDF {

	private final int DPI = 68;
	private final double border = 0.5;

	private PDRectangle pageSize = PDRectangle.A4;

	private String saveFileName;

	public ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>();

	private void setPageProperties(int imgWidthPixels, int imgHeightPixels) {

		double imgWidthInches = (double) imgWidthPixels / (double) DPI;
		double imgHeightInches = (double) imgHeightPixels / (double) DPI;

		if ((imgWidthInches < 8.27) && (imgHeightInches < 11.69))
			pageSize = PDRectangle.A4;
		else if ((imgWidthInches < 11.69) && (imgHeightInches < 16.54))
			pageSize = PDRectangle.A3;
		else if ((imgWidthInches < 16.54) && (imgHeightInches < 23.39))
			pageSize = PDRectangle.A2;
		else if ((imgWidthInches < 23.39) && (imgHeightInches < 33.11))
			pageSize = PDRectangle.A1;
		else
			pageSize = PDRectangle.A0;

	}

	private void getFileNameDialog(Shell shell) {

		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setFilterNames(new String[] { "PDF Files" });
		dialog.setFilterExtensions(new String[] { "*.pdf" });

		dialog.setFileName("mypdfscribe.pdf");
		saveFileName = dialog.open();
	}

	public void generatePDF(Shell shell) {

		try {
			getFileNameDialog(shell);

			// Create blank PDF document
			PDDocument doc = new PDDocument();
			PDPageContentStream contentStream = null;

			for (int i = 0; i < imageList.size(); i++) {

				PDImageXObject pdImage = LosslessFactory.createFromImage(doc, imageList.get(i));

				// Use first image to define the PDf page properties
				if (i == 0)
					setPageProperties(pdImage.getWidth(), pdImage.getHeight());

				PDPage page = new PDPage(pageSize);
				doc.addPage(page);

				contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true);
				PDRectangle cropBox = page.getCropBox();
				contentStream.drawImage(pdImage, (int) (border * DPI),
						cropBox.getHeight() - pdImage.getHeight() - (int) (border * DPI));
				contentStream.close();
			}
			
			if (saveFileName != null)
				doc.save(saveFileName);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
