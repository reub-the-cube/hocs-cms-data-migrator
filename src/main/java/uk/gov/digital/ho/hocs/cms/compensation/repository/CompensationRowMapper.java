package uk.gov.digital.ho.hocs.cms.compensation.repository;
import org.springframework.jdbc.core.RowMapper;
import uk.gov.digital.ho.hocs.cms.compensation.Compensation;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CompensationRowMapper implements RowMapper<Compensation> {

        @Override
        public Compensation mapRow(ResultSet rs, int rowNum) throws SQLException {
            Compensation compensation = new Compensation();
            compensation.setDateOfCompensationClaim(rs.getDate("dateofcompensationclaim").toLocalDate());
            compensation.setOfferAccepted(rs.getString("offeraccepted"));
            compensation.setDateOfPayment(rs.getDate("dateofpayment").toLocalDate());
            compensation.setCompensationAmmount(rs.getBigDecimal("compensationamount"));
            compensation.setAmountClaimed(rs.getBigDecimal("amountclaimed"));
            compensation.setAmountOffered(rs.getBigDecimal("amountoffered"));
            compensation.setConsolatoryPayment(rs.getBigDecimal("consolatorypayment"));
            return compensation;
        }
}
