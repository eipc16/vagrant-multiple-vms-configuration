package com.piisw.cinema_tickets_app.domain.genre.control;

import com.piisw.cinema_tickets_app.domain.auditedobject.entity.ObjectState;
import com.piisw.cinema_tickets_app.domain.genre.entity.Genre;
import com.piisw.cinema_tickets_app.infrastructure.bulk.BulkOperationResult;
import com.piisw.cinema_tickets_app.infrastructure.bulk.OperationResultEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public List<Genre> getGenresByName(String genreName, Set<ObjectState> objectStates) {
        return genreRepository.findAllByNameLikeAndObjectStateIn("%" + genreName + "%", objectStates);
    }

    public BulkOperationResult<Genre> createGenres(Collection<String> genreNames) {
        List<Genre> existingGenres = genreRepository.findAllByNameInAndObjectState(genreNames, ObjectState.ACTIVE);
        List<String> existingGenreNames = existingGenres.stream()
                .map(Genre::getName)
                .collect(Collectors.toList());
        List<Genre> createdGenres = genreNames.stream()
                .filter(genre -> !existingGenreNames.contains(genre))
                .map(this::buildGenre)
                .collect(Collectors.toList());
        return BulkOperationResult.<Genre>builder()
                .addAllResults(OperationResultEnum.CREATED, genreRepository.saveAll(createdGenres))
                .addAllResults(OperationResultEnum.NOT_CREATED, existingGenres)
                .build();
    }

    private Genre buildGenre(String genreName) {
        return Genre.builder()
                .name(genreName)
                .objectState(ObjectState.ACTIVE)
                .build();
    }
}
