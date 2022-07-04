package pl.grabojan.certsentry.tsldownload.data;

import pl.grabojan.certsentry.data.model.TrustedList;
import pl.grabojan.certsentry.schema.tsl.TrustServiceStatusList;

public interface TrustedListDataMapper {

	TrustedList createOrUpdateData(TrustedList dbTL, TrustServiceStatusList xmlTL);

}