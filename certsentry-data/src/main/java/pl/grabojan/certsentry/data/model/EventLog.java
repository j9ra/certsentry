package pl.grabojan.certsentry.data.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;

@Data
@Entity
@Table(name="EVENT_LOG")
public class EventLog implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="EVENT_LOG_SEQ")
	@Column(name="ID")
	private Long id;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="TIMESTAMP", nullable = false)
	private Date timestamp;
	
	@Enumerated(EnumType.STRING)
	@Column(name="TYPE", nullable = false)
	private EventType type;
	
	@Column(name="SOURCE", nullable = false)
	private String source;
	
	@Column(name="DESCRIPTION", nullable = false, length = 350)
	private String description;
	
	@JsonManagedReference
	@ManyToOne
	@JoinColumn(name="USERNAME_ID")
	private SecUser secUser;
	
}
