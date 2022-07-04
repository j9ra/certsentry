package pl.grabojan.certsentry.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name="PROVIDER")
public class Provider implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PROVIDER_SEQ")
	@Column(name="ID")
	private Long id;
	
	@Column(name="NAME", nullable = false)
	private String name;
	
	@Column(name="TRADE_NAME")
	private String tradeName;
	
	@Column(name="INFORMATION_URI", nullable = false)
	private String informationUri;
	
	@JsonBackReference
	@ToString.Exclude
	@ManyToOne
	@JoinColumn(name="TRUSTED_LIST_ID")
	private TrustedList trustedList;
	
	@JsonManagedReference
	@ToString.Exclude
	@OneToMany(mappedBy = "provider", orphanRemoval = true, cascade = { CascadeType.ALL })
	private Collection<Service> services = new ArrayList<>();
	
}
