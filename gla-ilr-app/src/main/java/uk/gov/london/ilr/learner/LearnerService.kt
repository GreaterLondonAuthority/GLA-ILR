package uk.gov.london.ilr.learner

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
@Transactional
class LearnerService @Autowired constructor(val learnerRepository: LearnerRepository,
                                            val learnerDeliveryRepository: LearningDeliveryRepository,
                                            val learnerSummaryRepository: LearnerSummaryRepository,
                                            val learningAimRepository: LearningAimRepository) {

    fun getLearnersSummaries(learner: String?, ukprns: Set<Int>?, academicYear: Int?, pageable: Pageable?): Page<LearnerSummary> {
        return learnerSummaryRepository.findAll(learner, ukprns, academicYear, pageable!!)
    }

    fun getLearnersAcademicYears(): List<Int> {
        return learnerRepository.findDistinctAcademicYears()
    }

    fun getLearnerRecord(learner: LearnerPK): Learner {
        return learnerRepository.findById(learner).get()
    }

    fun getLearningDeliveryForLeaner(learnerId: LearnerPK): List<LearningDelivery> {
        val learningDelivery = learnerDeliveryRepository.findAllByIdUkprnAndIdLearnerReferenceNumberAndIdYearOrderByIdAimSequenceNumber(learnerId.ukprn, learnerId.learnerReferenceNumber, learnerId.year)
        for (delivery in learningDelivery) {
            delivery.aim = learningAimRepository.findById(LearningAimPK(delivery.aimReference!!, learnerId.year)).orElse(null)
        }
        return learningDelivery
    }

    fun learnerRecordByYearAndPeriodExists(year: Int, period: Int): Boolean {
        return learnerRepository.recordByYearAndPeriodExists(year, period)
    }
}
