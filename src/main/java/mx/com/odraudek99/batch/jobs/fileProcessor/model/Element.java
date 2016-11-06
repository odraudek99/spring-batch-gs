package mx.com.odraudek99.batch.jobs.fileProcessor.model;

import java.io.Serializable;

public class Element implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int id;
	 
	private String text;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	

}
