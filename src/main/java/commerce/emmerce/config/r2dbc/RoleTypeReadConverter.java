package commerce.emmerce.config.r2dbc;

import commerce.emmerce.domain.RoleType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class RoleTypeReadConverter implements Converter<String, RoleType> {

    @Override
    public RoleType convert(String source) {
        return RoleType.valueOf(source);
    }

}
