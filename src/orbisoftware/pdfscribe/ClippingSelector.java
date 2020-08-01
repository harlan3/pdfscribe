package orbisoftware.pdfscribe;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tracker;

public class ClippingSelector {

	public Rectangle clipRect;

	public void showClippingSelector() {

		final Display display = Display.getDefault();
		final Shell shell = new Shell(display);

		clipRect = new ClippingSelector().select();

		if (clipRect.height == 0 || clipRect.width == 0)
			return;

		// we show the selected area in a new shell.
		final Image image = new Image(display, clipRect);
		final GC gc = new GC(display);
		gc.copyArea(image, clipRect.x, clipRect.y);
		gc.dispose();

		shell.setBounds(clipRect);
		shell.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				e.gc.drawImage(image, 0, 0);
			}
		});

		shell.open();
	}

	public Rectangle select() {

		final Display display = Display.getDefault();

		// convert desktop to image
		final Image backgroundImage = new Image(display, display.getBounds().width, display.getBounds().height);
		GC gc = new GC(display);
		gc.copyArea(backgroundImage, display.getBounds().x, display.getBounds().y);
		gc.dispose();

		// invisible shell and parent for tracker
		final Shell shell = new Shell(display.getActiveShell(), SWT.NO_BACKGROUND | SWT.ON_TOP);
		shell.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_CROSS));
		shell.setBounds(display.getBounds());

		final Rectangle result = new Rectangle(0, 0, 0, 0);

		shell.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {

				Tracker tracker = new Tracker(shell, SWT.RESIZE);
				tracker.setStippled(true);
				tracker.setRectangles(new Rectangle[] { new Rectangle(e.x, e.y, 0, 0) });
				tracker.open();

				Rectangle selection = tracker.getRectangles()[0];

				result.width = selection.width;
				result.height = selection.height;

				result.x = shell.toDisplay(selection.x, selection.y).x;
				result.y = shell.toDisplay(selection.x, selection.y).y;

				shell.dispose();

			}
		});

		shell.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.drawImage(backgroundImage, -1, -1); // paint background image on invisible shell
			}
		});

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		backgroundImage.dispose();
		shell.dispose();

		return result;
	}

}