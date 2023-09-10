package ru.yandex.practicum.filmorate.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Service
public class FilmService implements FilmStorage {
    private final FilmStorage filmStorage;
    private final Validator validator;

    public FilmService(@Qualifier("filmDaoStorage") FilmStorage filmStorage, Validator validator) {
        this.filmStorage = filmStorage;
        this.validator = validator;
    }

    public void addLike(long id, long userId) {
        filmStorage.addLike(id, userId);
    }

    public void deleteLike(long id, long userId) {
        filmStorage.deleteLike(id, userId);
    }

    @Override
    public Film createFilm(@Valid Film film) {
        if (validator.validateFilm(film)) {
            filmStorage.createFilm(film);
            log.debug("Сохранен фильм: {}", film);
        }
        return film;
    }

    public List<Film> getMostPopularFilms(int count) {
        if (count > 0) {
            return filmStorage.getMostPopularFilms(count);
        } else {
            log.warn("Ошибка запроса списка популярных фильмов.");
            throw new ValidationException("Ошибка запроса списка популярных фильмов, проверьте корректность данных.");
        }
    }

    @Override
    public List<Film> allFilms() {
        return filmStorage.allFilms();
    }

    @Override
    public Film getFilmById(long id) {
        try {
            return filmStorage.getFilmById(id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Ошибка запроса фильма.");
            throw new ResourceNotFoundException("Ошибка запроса фильма, проверьте корректность данных.");
        }
    }

    @Override
    public Film updateFilm(@NonNull Film film) {
        if (getFilmById(film.getId()).getId() == film.getId()
                && validator.validateFilm(film)
                && validator.validateFilmId(filmStorage.allFilms().size(), film.getId())) {
            filmStorage.updateFilm(film);
            log.debug("Обновлен фильм: {}", film);
            return film;
        } else {
            log.warn("Ошибка при обновлении фильма: {}", film);
            throw new ResourceNotFoundException("Ошибка при изменении фильма, проверьте корректность данных.");
        }
    }

    @Override
    public void deleteFilm(long id) {
        if (id > 0) {
            filmStorage.deleteFilm(id);
            log.warn("Фильм удалён.");
        } else {
            log.warn("Ошибка при удалении фильма с id: {}", filmStorage.getFilmById(id));
            throw new ResourceNotFoundException("Ошибка при удалении фильма, проверьте корректность id фильма.");
        }
    }

}
