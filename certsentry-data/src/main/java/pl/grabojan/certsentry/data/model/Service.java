package pl.grabojan.certsentry.data.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name="SERVICE")
public class Service implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SERVICE_SEQ")
	@Column(name="ID")
	private Long id;
	
	@Enumerated(EnumType.STRING)
	@Column(name="TYPE", nullable = false)
	private ServiceType type;
	
	@Column(name="NAME", nullable = false)
	private String name;
	
	@Enumerated(EnumType.STRING)
	@Column(name="STATUS", nullable = false)
	private ServiceStatus status;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="START_DATE", nullable = false)
	private Date startDate;
	
	@Column(name="DEFINITION_URI")
	private String definitionUri;
	
	@JsonBackReference
	@ToString.Exclude
	@ManyToOne
	@JoinColumn(name="PROVIDER_ID")
	private Provider provider;
	
	@JsonManagedReference
	@ToString.Exclude
	@OneToMany(mappedBy="service", orphanRemoval = true, cascade = { CascadeType.ALL })
	private Collection<CertIdentity> certIdentities = new LinkedList<>();
	
	@JsonManagedReference
	@ToString.Exclude
	@OneToMany(mappedBy="service",  orphanRemoval = true, cascade = { CascadeType.ALL })
	private Collection<SupplyPoint> supplyPoints = new LinkedList<>();
	
	@JsonManagedReference
	@ToString.Exclude
	@OneToMany(mappedBy="service",  orphanRemoval = true, cascade = { CascadeType.ALL })
	private Collection<ServiceExtension> extensions = new LinkedList<>();
}
