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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name="TRUSTED_LIST")
public class TrustedList implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="TRUSTED_LIST_SEQ")
	@Column(name="ID")
	private Long id;
	
	@Enumerated(EnumType.STRING)
	@Column(name="TYPE", nullable = false)
	private TrustedListType type;
	
	@Column(name="SEQUENCE_NUMBER", nullable = false)
	private Long sequenceNumber;
	
	@Column(name="OPERATOR_NAME", nullable = false)
	private String operatorName;
	
	@Column(name="DISTRIBUTION_POINT")
	private String distributionPoint;
	
	@Column(name="INFORMATION_URI")
	private String informationUri;
	
	@Column(name="TERRITORY", nullable = false)
	private String territory;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LIST_ISSUE", nullable = false)
	private Date listIssue;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="NEXT_UPDATE", nullable = false)
	private Date nextUpdate;
	
	@Column(name="LIST_HASH")
	private String listHash;
	
	@Column(name="LOCAL_URI")
	private String localUri;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_CHECK")
	private Date lastCheck;
	
	@Column(name="IS_VALID")
	private Boolean isValid;
	
	@JsonManagedReference
	@ToString.Exclude
	@OneToMany(mappedBy = "trustedList", orphanRemoval = true, cascade = { CascadeType.ALL })
	private Collection<Provider> providers = new LinkedList<>();
	
	@JsonManagedReference
	@ToString.Exclude
	@OneToMany(mappedBy = "trustedList")
	private Collection<TrustedListUpdate> trustedListUpdates = new LinkedList<>();
}
