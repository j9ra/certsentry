package pl.grabojan.certsentry.data.model;

import java.io.Serializable;

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

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;

@Data
@Entity
@Table(name="EXTENSION")
public class ServiceExtension implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="EXTENSION_SEQ")
	@Column(name="ID")
	private Long id;
	
	@Column(name="NAME", nullable = false)
	private String name;
	
	@Enumerated(EnumType.STRING)
	@Column(name="TYPE", nullable = false)
	private ExtensionType type;
	
	@Column(name="VALUE", nullable = false)
	private String value;
	
	@JsonBackReference
	@ManyToOne
	@JoinColumn(name="SERVICE_ID")
	private Service service;
}
