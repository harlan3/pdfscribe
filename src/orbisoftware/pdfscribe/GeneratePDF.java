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

	public PDRectangle pageSize = PDRectangle.A4;
	public float borderPercent = 0.0f;

	public ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>();

	private String saveFileName;

	public void setPageSize(String paperSizeString) {

		switch (paperSizeString) {

		case "A0":
			pageSize = PDRectangle.A0;
			break;

		case "A1":
			pageSize = PDRectangle.A1;
			break;

		case "A2":
			pageSize = PDRectangle.A2;
			break;

		case "A3":
			pageSize = PDRectangle.A3;
			break;

		case "A4":
			pageSize = PDRectangle.A4;
			break;

		case "A5":
			pageSize = PDRectangle.A5;
			break;

		case "A6":
			pageSize = PDRectangle.A6;
			break;

		case "Letter":
			pageSize = PDRectangle.LETTER;
			break;

		case "Legal":
			pageSize = PDRectangle.LEGAL;
			break;

		}

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
			float scaleFactor = 1.0f;
			float scaleFactorX = 1.0f;
			float scaleFactorY = 1.0f;
			int borderPixelsWidth = 0;
			int borderPixelsHeight = 0;

			for (int i = 0; i < imageList.size(); i++) {

				System.out.println("Processing image: " + (i + 1));

				PDImageXObject pdImage = LosslessFactory.createFromImage(doc, imageList.get(i));

				PDPage page = new PDPage(pageSize);
				doc.addPage(page);

				contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true);
				PDRectangle cropBox = page.getCropBox();

				borderPixelsWidth = (int) (cropBox.getWidth() * borderPercent);
				borderPixelsHeight = (int) (cropBox.getHeight() * borderPercent);

				scaleFactorX = (cropBox.getWidth() - (2 * borderPixelsWidth)) / pdImage.getWidth();
				scaleFactorY = (cropBox.getHeight() - (2 * borderPixelsHeight)) / pdImage.getHeight();

				if (scaleFactorX < scaleFactorY)
					scaleFactor = scaleFactorX;
				else
					scaleFactor = scaleFactorY;

				contentStream.drawImage(pdImage, borderPixelsWidth,
						cropBox.getHeight() - (pdImage.getHeight() * scaleFactor) - borderPixelsHeight,
						pdImage.getWidth() * scaleFactor, pdImage.getHeight() * scaleFactor);
				contentStream.close();
			}

			System.out.println("Saving to: " + saveFileName);
			if (saveFileName != null)
				doc.save(saveFileName);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
