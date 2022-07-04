package pl.grabojan.certsentry.admin.rest.dto;

import lombok.Data;

@Data
public class Metrics {

	private Long users;
	private Long profiles;
	private Long trustLists;
	private Long events;
	
	public static Metrics createDefault() {
		return new Metrics(0,0,0,0);
	}

	protected Metrics(long users, long profiles, long trustLists, long events) {
		this.users = users;
		this.profiles = profiles;
		this.trustLists = trustLists;
		this.events = events;
	}
}
