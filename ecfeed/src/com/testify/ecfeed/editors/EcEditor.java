package com.testify.ecfeed.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextEditor;

import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.testify.ecfeed.outline.EcContentOutlinePage;

public class EcEditor extends TextEditor{
	
	private ColorManager fColorManager;
	private EcContentOutlinePage fContentOutline;
	
	public EcEditor(){
		super();
		fColorManager = new ColorManager();
		setSourceViewerConfiguration(new EcViewerConfiguration(fColorManager));
		setDocumentProvider(new EcDocumentProvider());
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class required){
		if(IContentOutlinePage.class.equals(required)){
			if(fContentOutline == null){
				fContentOutline = new EcContentOutlinePage(this);
				fContentOutline.setTextEditor(this);
			}
			return fContentOutline;
		}
		return super.getAdapter(required);
	}
	
	public void dispose() {
		fColorManager.dispose();
		super.dispose();
	}
	
	public IDocument getDocument(){
		return getSourceViewer().getDocument();
	}
}
