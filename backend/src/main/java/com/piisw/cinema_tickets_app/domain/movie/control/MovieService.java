package com.piisw.cinema_tickets_app.domain.movie.control;

import com.google.common.collect.Sets;
import com.piisw.cinema_tickets_app.api.MovieDetailsDTO;
import com.piisw.cinema_tickets_app.client.OpenMovieDatabaseClient;
import com.piisw.cinema_tickets_app.domain.auditedobject.control.AuditedObjectSpecification;
import com.piisw.cinema_tickets_app.domain.auditedobject.entity.ObjectState;
import com.piisw.cinema_tickets_app.domain.genre.control.GenreService;
import com.piisw.cinema_tickets_app.domain.genre.entity.Genre;
import com.piisw.cinema_tickets_app.domain.movie.entity.Movie;
import com.piisw.cinema_tickets_app.infrastructure.bulk.BulkOperationResult;
import com.piisw.cinema_tickets_app.infrastructure.bulk.OperationResultEnum;
import com.piisw.cinema_tickets_app.infrastructure.utils.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private AuditedObjectSpecification<Movie> specification;

    @Autowired
    private OpenMovieDatabaseClient openMovieDatabaseClient;

    @Autowired
    private GenreService genreService;

    @Autowired
    private MovieToGenreRelationService movieToGenreRelationService;

    public Movie getMovieById(Long id, ObjectState objectState) {
        return movieRepository.findOne(specification.whereIdAndObjectStateEquals(id, objectState))
                .orElseThrow(() -> ExceptionUtils.getObjectNotFoundException(Movie.class, id, objectState));
    }

    public Movie getMovieById(Long id, Set<ObjectState> objectStates) {
        return movieRepository.findOne(specification.whereIdEqualsAndObjectStateIn(id, objectStates))
                .orElseThrow(() -> ExceptionUtils.getObjectNotFoundException(Movie.class, id, objectStates));
    }

    public List<Movie> getMoviesByIds(Set<Long> ids, Set<ObjectState> objectStates) {
        return movieRepository.findAll(specification.whereIdAndObjectStateIn(ids, objectStates));
    }

    @Transactional
    public BulkOperationResult<Movie> createMovies(Set<String> imdbIds) {
        List<Movie> existingMovies = movieRepository.findAllByImdbIdInAndObjectState(imdbIds, ObjectState.ACTIVE);
        List<String> existingMoviesImdbIds = existingMovies.stream()
                .map(Movie::getImdbId)
                .collect(Collectors.toUnmodifiableList());
        List<Movie> moviesToCreate = openMovieDatabaseClient.getMovieDetailsByImdbIds(imdbIds).stream()
                .filter(movie -> !existingMoviesImdbIds.contains(movie.getImdbId()))
                .map(this::buildMovieEntity)
                .collect(Collectors.toList());
        movieToGenreRelationService.createRelations(moviesToCreate);
        return BulkOperationResult.<Movie>builder()
                .addAllResults(OperationResultEnum.CREATED, movieRepository.saveAll(moviesToCreate))
                .addAllResults(OperationResultEnum.NOT_CREATED, existingMovies)
                .build();
    }

    private Movie buildMovieEntity(MovieDetailsDTO movieDetailsDTO) {
        Set<Genre> genres = genreService.createGenres(movieDetailsDTO.getGenres()).getAll();
        return Movie.builder()
                .imdbId(movieDetailsDTO.getImdbId())
                .title(movieDetailsDTO.getTitle())
                .year(movieDetailsDTO.getYear())
                .maturityRating(movieDetailsDTO.getMaturityRate())
                .releaseDate(movieDetailsDTO.getReleaseDate().atStartOfDay().toInstant(ZoneOffset.UTC))
                .runTime(movieDetailsDTO.getRuntime())
                .director(movieDetailsDTO.getDirector())
                .actors(movieDetailsDTO.getActors())
                .shortPlot(movieDetailsDTO.getPlot())
                .language(movieDetailsDTO.getLanguage())
                .posterUrl(movieDetailsDTO.getPosterLink())
                .country(movieDetailsDTO.getCountry())
                .objectState(ObjectState.ACTIVE)
                .genres(genres)
                .build();
    }

    @Transactional
    public List<Movie> deleteMoviesByIds(Set<Long> ids) {
        List<Movie> moviesToRemove = movieRepository.findAll(specification.whereIdIn(ids));
        validateIfAllMoviesToRemoveExists(ids, moviesToRemove);
        moviesToRemove.forEach(movie -> movie.setObjectState(ObjectState.REMOVED));
        movieToGenreRelationService.removeRelations(moviesToRemove);
        return movieRepository.saveAll(moviesToRemove);
    }

    private void validateIfAllMoviesToRemoveExists(Set<Long> idsOfMoviesToRemove, List<Movie> foundMovies) {
        Set<Long> idsOfNonExistingMovies = getIdsOfNonExistingMovies(idsOfMoviesToRemove, foundMovies);
        if (!idsOfNonExistingMovies.isEmpty()) {
            throw ExceptionUtils.getObjectNotFoundException(Movie.class, idsOfNonExistingMovies, ObjectState.ACTIVE);
        }
    }

    private Set<Long> getIdsOfNonExistingMovies(Set<Long> idsOfMoviesToRemove, List<Movie> foundMovies) {
        return foundMovies.stream()
                .map(Movie::getId)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), foundIds -> Sets.difference(idsOfMoviesToRemove, foundIds)));
    }

}