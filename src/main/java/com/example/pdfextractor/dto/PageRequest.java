package com.example.pdfextractor.dto;

public class PageRequest {
	private String nameDoc;
	private String pag;
	
	public String getNameDoc() {
        return nameDoc;
    }

    public void setNameDoc(String nameDoc) {
        this.nameDoc = nameDoc;
    }

    public String getPag() {
        return pag;
    }

    public void setPag(String pag) {
        this.pag = pag;
    }
}
