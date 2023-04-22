

-- INSERT 5 DATA FIXTURES
INSERT INTO LIBRARY_EVENT_ENTITY (library_event_id, library_event_type) VALUES (2, 'NEW');
INSERT INTO LIBRARY_EVENT_ENTITY (library_event_id, library_event_type) VALUES (3, 'NEW');
INSERT INTO LIBRARY_EVENT_ENTITY (library_event_id, library_event_type) VALUES (4, 'NEW');
INSERT INTO LIBRARY_EVENT_ENTITY (library_event_id, library_event_type) VALUES (5, 'NEW');

INSERT INTO BOOK_ENTITY (book_id, book_author, book_name, library_event_id) VALUES (2, 'Shakespear','English in the world',2);
INSERT INTO BOOK_ENTITY (book_id, book_author, book_name, library_event_id) VALUES (3, 'James Gosling','Best of Java',3);


