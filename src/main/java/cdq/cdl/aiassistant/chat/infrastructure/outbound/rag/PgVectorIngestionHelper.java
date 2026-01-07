package cdq.cdl.aiassistant.chat.infrastructure.outbound.rag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PgVectorIngestionHelper
{
    private final DataSource dataSource;

    boolean tableExistsAndNotEmpty()
    {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM information_schema.tables
                    WHERE table_schema = 'public'
                      AND table_name = ?
                )
                AND EXISTS (
                    SELECT 1 FROM %s LIMIT 1
                )
                """.formatted("cdq_product_knowledge");

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql))
        {

            ps.setString(1, "cdq_product_knowledge");

            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next() && rs.getBoolean(1);
            }

        }
        catch (Exception e)
        {
            log.warn("Could not determine table state for {}, assuming empty", "cdq_product_knowledge", e);
            return false;
        }
    }
}
