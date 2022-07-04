package pl.grabojan.certsentry.admin.rest.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class GraphData<L, V> {

	private List<L> labels = new ArrayList<>();
	private List<V> values = new ArrayList<>();
		
	public GraphData() {
	
	}
	
	public void addLabelValue(L label, V value) {
		labels.add(label);
		values.add(value);
	}
	
}
