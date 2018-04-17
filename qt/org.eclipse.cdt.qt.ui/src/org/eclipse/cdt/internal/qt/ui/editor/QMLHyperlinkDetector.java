/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

public class QMLHyperlinkDetector extends AbstractHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		// TODO is length of region ever > 0?
		IRegion wordRegion = QMLEditor.findWord(textViewer.getDocument(), region.getOffset());
		if (wordRegion != null) {
			ITextEditor editor = (ITextEditor) getAdapter(ITextEditor.class);
			return new IHyperlink[] { new QMLHyperlink(wordRegion, textViewer, editor) };
		}
		return null;
	}

}
