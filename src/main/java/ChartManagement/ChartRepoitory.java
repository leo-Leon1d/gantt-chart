package ChartManagement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// Репозиторий
@Repository
public class ChartRepoitory {

    private final JdbcTemplate jdbcTemplate;

    // Конструктор
    public ChartRepoitory(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Создание диаграммы (таблицы SQL)
    public void createChart(Long id) {
        String sql = "CREATE TABLE IF NOT EXISTS ? (\n" +
                "id SERIAL PRIMARY KEY,\n" +
                "doers VARCHAR(40),\n" +
                "tasks VARCHAR(40),\n" +
                 ");";
        String name = "chart_" + id;
        jdbcTemplate.update(sql, name);
    }
}
