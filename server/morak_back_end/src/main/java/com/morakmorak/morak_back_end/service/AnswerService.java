package com.morakmorak.morak_back_end.service;

import com.morakmorak.morak_back_end.dto.AnswerDto;
import com.morakmorak.morak_back_end.entity.Answer;
import com.morakmorak.morak_back_end.entity.Article;
import com.morakmorak.morak_back_end.entity.File;
import com.morakmorak.morak_back_end.entity.User;
import com.morakmorak.morak_back_end.exception.BusinessLogicException;
import com.morakmorak.morak_back_end.exception.ErrorCode;
import com.morakmorak.morak_back_end.repository.AnswerRepository;
import com.morakmorak.morak_back_end.service.auth_user_service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AnswerService {
    private final ArticleService articleService;
    private final UserService userService;
    private final CommentService commentService;
    private final AnswerRepository answerRepository;

    public AnswerDto.SimpleResponsePostAnswer postAnswer(Long articleId, Long userId, Answer answerNotSaved, List<File> fileList) throws Exception {
        User verifiedUser = userService.findVerifiedUserById(userId);
        Article verifiedArticle = articleService.findVerifiedArticle(articleId);

        if (verifiedArticle.isQuestion() && !verifiedArticle.isClosedArticle()
                && commentService.isEditableStatus(verifiedArticle)) {
            Answer savedAnswer = answerRepository.save(injectAllInto(answerNotSaved, verifiedUser, verifiedArticle, fileList));
            return AnswerDto.SimpleResponsePostAnswer.of(savedAnswer);
        } else {
            throw new BusinessLogicException(ErrorCode.UNABLE_TO_ANSWER);
        }
    }

    private Answer injectAllInto(Answer answerNotSaved, User verifiedUser, Article verifiedArticle, List<File> fileList) {
        attachFilesToAnswer(answerNotSaved, fileList);
        answerNotSaved.injectUser(verifiedUser).injectArticle(verifiedArticle);
        return answerNotSaved;
    }

    public void attachFilesToAnswer(Answer answer, List<File> fileList) {
        fileList.stream().forEach(file -> {
            file.attachToAnswer(answer);
        });
    }
}
