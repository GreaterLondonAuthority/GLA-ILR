/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.ops;

import static uk.gov.london.ilr.file.FileUploadHandlerKt.getSkillsGrantType;
import static uk.gov.london.ilr.feature.IlrFeature.OPS_CONNECTION;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.togglz.core.manager.FeatureManager;
import uk.gov.london.common.JSONUtils;
import uk.gov.london.common.error.ApiError;
import uk.gov.london.common.skills.FundingRecord;
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ilr.file.DataImport;
import uk.gov.london.ilr.file.DataImportService;
import uk.gov.london.ilr.fundingsummary.FundingSummaryRecord;
import uk.gov.london.ilr.fundingsummary.FundingSummaryRecordRepository;
import uk.gov.london.ilr.security.SGWAuthenticationException;
import uk.gov.london.ilr.security.User;
import uk.gov.london.ilr.security.UserService;

@Service
public class OpsService {

    Logger log = LoggerFactory.getLogger(getClass());

    public static String[] TEST_USERS = new String[]{};

    @Autowired
    private FeatureManager features;

    @Autowired
    private FundingSummaryRecordRepository fundingSummaryRecordRepository;

    @Autowired
    private RestTemplate opsRestTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private DataImportService dataImportService;

    @Value("${ops.base.url}")
    String opsBaseUrl;

    @Value("${ops.funding.summary.api.path}")
    String opsFundingSummaryApiPath;

    @Value("${ops.funding.authentication.api.path}")
    String opsAuthenticationApiPath;

    public void pushFundingSummaryToOps(int recordId)  {
        DataImport dataImportRecord = dataImportService.getDataImportRecord(recordId);
        dataImportRecord.setLastExportDate(OffsetDateTime.now());
        Integer academicYear = dataImportRecord.getAcademicYear();
        Integer period = dataImportRecord.getPeriod();

        List<FundingSummaryRecord> fundingLearnerRecords =
                fundingSummaryRecordRepository.findAllByAcademicYearAndPeriod(academicYear, period);
        Set<FundingRecord> summaryList = fundingSummaryRecordsToCommonFormat(fundingLearnerRecords);

        if (!fundingLearnerRecords.isEmpty() && features.isActive(OPS_CONNECTION)) {
            try {
                opsRestTemplate.postForEntity(
                        opsBaseUrl + opsFundingSummaryApiPath
                                + "?academicYear=" + academicYear
                                + "&period=" + period,
                        summaryList,
                        String.class);

                dataImportService.updateDataImportRecord(dataImportRecord);

            } catch (HttpClientErrorException e) {
                String errorMessageFromOps = JSONUtils.fromJSON(e.getResponseBodyAsString(), ApiError.class).getDescription();
                throw new RuntimeException(errorMessageFromOps);
            } catch (Exception e) {
                log.error("failed to connect to OPS", e);
                throw e;
            }
        }
    }

    Set<FundingRecord> fundingSummaryRecordsToCommonFormat(List<FundingSummaryRecord> fundingSummaryRecords) {
        Set<FundingRecord> records = new HashSet<>();

        for (FundingSummaryRecord summary : fundingSummaryRecords) {
            SkillsGrantType recordGrantType = getSkillsGrantType(summary.getFundingLine());

            FundingRecord record = new FundingRecord(summary.getUkprn(), summary.getAcademicYear(), summary.getPeriod(),
                summary.getActualYear(), summary.getActualMonth(), recordGrantType, summary.getFundingLine(), summary.getSource(),
                summary.getCategory(), summary.getMonthTotal(), summary.getTotalPayment());
            records.add(record);
        }

        return records;
    }


    public User authenticate(String username, String password) {
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        if (Arrays.asList(TEST_USERS).contains(username)) {
            return (User) userService.loadUserByUsername(username);
        }

        try {
            String authUrl = opsBaseUrl + opsAuthenticationApiPath;
            ResponseEntity<User> response = opsRestTemplate.postForEntity(authUrl, credentials, User.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new SGWAuthenticationException(response.getStatusCodeValue());
            }
        } catch (HttpClientErrorException e) {
            throw new SGWAuthenticationException(e.getRawStatusCode());
        }
    }

}
