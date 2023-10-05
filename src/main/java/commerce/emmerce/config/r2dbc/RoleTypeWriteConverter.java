package commerce.emmerce.config.r2dbc;

import commerce.emmerce.domain.RoleType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class RoleTypeWriteConverter implements Converter<RoleType, String> {

    @Override
    public String convert(RoleType source) {
        return source.name();
    }

}
