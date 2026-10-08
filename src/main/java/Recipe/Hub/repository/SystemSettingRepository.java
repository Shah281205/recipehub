package Recipe.Hub.repository;

import Recipe.Hub.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository
        extends JpaRepository<SystemSetting, Long> {

    SystemSetting findFirstByOrderByIdAsc();

}