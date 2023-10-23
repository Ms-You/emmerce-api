package commerce.emmerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {
    private List<T> content;
    private int pageNumber;
    private int totalPages;
    private int totalElements;
    private boolean first;
    private boolean last;

    public PageResponseDTO (List<T> content, int page, int size, int totalElements) {
        this.content = content;
        this.pageNumber = page;
        this.totalPages = (int)Math.ceil((double)totalElements / size);
        this.totalElements = totalElements;
        this.first = page == 1;
        this.last = (page * size) >= totalElements;
    }
}
