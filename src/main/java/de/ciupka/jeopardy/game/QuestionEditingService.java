package de.ciupka.jeopardy.game;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.ciupka.jeopardy.configs.Views;

@Service
public class QuestionEditingService {

    private List<Category> board;
    private String masterPassword;

    public QuestionEditingService() throws IOException {
        this.board = GameService.readQuestions();
        this.masterPassword = GameService.readPassword();
    }

    public List<Category> getBoard() {
        return board;
    }

    public String getMasterPassword() {
        return masterPassword;
    }

    public void save() throws StreamReadException, DatabindException, IOException {
        File file = new File(GameService.getGameFolder(), "questions.json");
        ObjectWriter writer = new ObjectMapper().writerWithView(Views.Storage.class);
        writer.writeValue(file, this.board);;
    }
}
