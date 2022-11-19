package com.morakmorak.morak_back_end.Integration.article.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.morakmorak.morak_back_end.dto.AvatarDto;
import com.morakmorak.morak_back_end.dto.CommentDto;
import com.morakmorak.morak_back_end.dto.UserDto;
import com.morakmorak.morak_back_end.entity.*;
import com.morakmorak.morak_back_end.entity.enums.CategoryName;
import com.morakmorak.morak_back_end.entity.enums.Grade;
import com.morakmorak.morak_back_end.entity.enums.TagName;
import com.morakmorak.morak_back_end.repository.*;
import com.morakmorak.morak_back_end.security.util.JwtTokenUtil;
import com.morakmorak.morak_back_end.service.ArticleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static com.morakmorak.morak_back_end.util.SecurityTestConstants.JWT_HEADER;
import static com.morakmorak.morak_back_end.util.SecurityTestConstants.ROLE_USER_LIST;
import static com.morakmorak.morak_back_end.util.TestConstants.EMAIL1;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Transactional
@SpringBootTest(value = {
        "jwt.secretKey=only_test_secret_Key_value_gn..rlfdlrkqnwhrgkekspdy",
        "jwt.refreshKey=only_test_refresh_key_value_gn..rlfdlrkqnwhrgkekspdy"
})
@AutoConfigureMockMvc
public class ArticleGetTest {
    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @PersistenceContext
    EntityManager em;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RedisRepository<String> mailAuthRedisRepository;

    @Autowired
    ArticleService articleService;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ArticleTagRepository articleTagRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ArticleRepository articleRepository;


    @Test
    @DisplayName("게시글을 타이틀명과 카테고리로 검색 성공시 201코드와 Ok를 반환한다.")
    public void searchArticleTest_title() throws Exception {
        Tag JAVA = Tag.builder().name(TagName.JAVA).build();
        em.persist(JAVA);

        Category info = Category.builder().name(CategoryName.INFO).build();
        em.persist(info);

        Avatar avatar = Avatar.builder().remotePath("remotePath")
                .originalFilename("fileName")
                .build();
        em.persist(avatar);

        User user = User.builder().nickname("nickname").grade(Grade.BRONZE).avatar(avatar).build();

        List<ArticleLike> articleLikes = new ArrayList<>();

        ArticleTag articleTagJava = ArticleTag.builder().tag(JAVA).build();

        Article article = Article.builder().title("테스트 타이틀입니다. 잘부탁드립니다. 제발 돼라!!!~~~~~~~~")
                .content("콘탠트입니다. 제발 됬으면 좋겠습니다.")
                .articleTags(List.of(articleTagJava))
                .category(info)
                .articleLikes(articleLikes)
                .user(user)
                .build();
        info.getArticleList().add(article);
        articleTagJava.injectMappingForArticleAndTag(article);
        em.persist(article);

        user.getArticles().add(article);
        em.persist(user);

        //when
        ResultActions perform = mockMvc.perform(
                get("/articles")
                        .param("category", "INFO")
                        .param("keyword", "테스트 타이틀입니다. 잘부탁드립니다. 제발 돼라!!!~~~~~~~~")
                        .param("target", "title")
                        .param("sort", "desc")
                        .param("page", "1")
                        .param("size", "1")
        );

        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0:1].articleId").value(article.getId().intValue()))
                .andExpect(jsonPath("$.data[0:1].category").value("INFO"))
                .andExpect(jsonPath("$.data[0:1].title").value("테스트 타이틀입니다. 잘부탁드립니다. 제발 돼라!!!~~~~~~~~"))
                .andExpect(jsonPath("$.data[0:1].clicks").value(0))
                .andExpect(jsonPath("$.data[0:1].likes").value(0))
                .andExpect(jsonPath("$.data[0:1].isClosed").value(false))
                .andExpect(jsonPath("$.data[0:1].commentCount").value(0))
                .andExpect(jsonPath("$.data[0:1].answerCount").value(0))
                .andExpect(jsonPath("$.data[0:1].createdAt").exists())
                .andExpect(jsonPath("$.data[0:1].lastModifiedAt").exists())
                .andExpect(jsonPath("$.data[0:1].tags[0:1].tagId").value(JAVA.getId().intValue()))
                .andExpect(jsonPath("$.data[0:1].tags[0:1].name").value("JAVA"))
                .andExpect(jsonPath("$.data[0:1].userInfo.userId").value(user.getId().intValue()))
                .andExpect(jsonPath("$.data[0:1].userInfo.nickname").value("nickname"))
                .andExpect(jsonPath("$.data[0:1].userInfo.grade").value("BRONZE"))
                .andExpect(jsonPath("$.data[0:1].avatar.avatarId").value(avatar.getId().intValue()))
                .andExpect(jsonPath("$.data[0:1].avatar.filename").value("fileName"))
                .andExpect(jsonPath("$.data[0:1].avatar.remotePath").value("remotePath"))
                .andExpect(jsonPath("$.pageInfo.page").value(1))
                .andExpect(jsonPath("$.pageInfo.size").value(1))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(1))
                .andExpect(jsonPath("$.pageInfo.sort.sorted").value(false));
    }

    @Test
    @DisplayName("게시글을 태그명과 카테고리로 검색 성공시 201코드와 Ok를 반환한다.")
    public void searchArticleTest_tag() throws Exception {
        Tag JAVA = Tag.builder().name(TagName.JAVA).build();
        em.persist(JAVA);

        Category info = Category.builder().name(CategoryName.INFO).build();
        em.persist(info);

        Avatar avatar = Avatar.builder().remotePath("remotePath")
                .originalFilename("fileName")
                .build();
        em.persist(avatar);

        User user = User.builder().nickname("nickname").grade(Grade.BRONZE).avatar(avatar).build();

        List<ArticleLike> articleLikes = new ArrayList<>();

        ArticleTag articleTagJava = ArticleTag.builder().tag(JAVA).build();

        Article article = Article.builder().title("테스트 타이틀입니다. 잘부탁드립니다. 제발 돼라!!!~~~~~~~~")
                .content("콘탠트입니다. 제발 됬으면 좋겠습니다.")
                .articleTags(List.of(articleTagJava))
                .category(info)
                .articleLikes(articleLikes)
                .user(user)
                .build();
        info.getArticleList().add(article);
        articleTagJava.injectMappingForArticleAndTag(article);
        em.persist(article);

        user.getArticles().add(article);
        em.persist(user);

        //when
        ResultActions perform = mockMvc.perform(
                get("/articles")
                        .param("category", "INFO")
                        .param("keyword", "JAVA")
                        .param("target", "tag")
                        .param("sort", "desc")
                        .param("page", "1")
                        .param("size", "1")
        );

        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0:1].articleId").value(article.getId().intValue()))
                .andExpect(jsonPath("$.data[0:1].category").value("INFO"))
                .andExpect(jsonPath("$.data[0:1].title").value("테스트 타이틀입니다. 잘부탁드립니다. 제발 돼라!!!~~~~~~~~"))
                .andExpect(jsonPath("$.data[0:1].clicks").value(0))
                .andExpect(jsonPath("$.data[0:1].likes").value(0))
                .andExpect(jsonPath("$.data[0:1].isClosed").value(false))
                .andExpect(jsonPath("$.data[0:1].commentCount").value(0))
                .andExpect(jsonPath("$.data[0:1].answerCount").value(0))
                .andExpect(jsonPath("$.data[0:1].createdAt").exists())
                .andExpect(jsonPath("$.data[0:1].lastModifiedAt").exists())
                .andExpect(jsonPath("$.data[0:1].tags[0:1].tagId").value(JAVA.getId().intValue()))
                .andExpect(jsonPath("$.data[0:1].tags[0:1].name").value("JAVA"))
                .andExpect(jsonPath("$.data[0:1].userInfo.userId").value(user.getId().intValue()))
                .andExpect(jsonPath("$.data[0:1].userInfo.nickname").value("nickname"))
                .andExpect(jsonPath("$.data[0:1].userInfo.grade").value("BRONZE"))
                .andExpect(jsonPath("$.data[0:1].avatar.avatarId").value(avatar.getId().intValue()))
                .andExpect(jsonPath("$.data[0:1].avatar.filename").value("fileName"))
                .andExpect(jsonPath("$.data[0:1].avatar.remotePath").value("remotePath"))
                .andExpect(jsonPath("$.pageInfo.page").value(1))
                .andExpect(jsonPath("$.pageInfo.size").value(1))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(1))
                .andExpect(jsonPath("$.pageInfo.sort.sorted").value(false));
    }

    @Test
    @DisplayName("게시글 상세조회 컨트롤러 이용시 jwt토큰을 보낼시 isBookmarked와 isLiked를 확인하고 true or false를 보낸다.")
    public void findDetailArticle_suc() throws Exception {
    //given
        Tag JAVA = Tag.builder().name(TagName.JAVA).build();
        em.persist(JAVA);

        Category info = Category.builder().name(CategoryName.INFO).build();
        em.persist(info);

        Avatar avatar = Avatar.builder().remotePath("remotePath")
                .originalFilename("fileName")
                .build();
        em.persist(avatar);

        User user = User.builder().email(EMAIL1).nickname("nickname").grade(Grade.BRONZE).avatar(avatar).build();


        ArticleTag articleTagJava = ArticleTag.builder().tag(JAVA).build();



        Article article = Article.builder().title("테스트 타이틀입니다. 잘부탁드립니다. 제발 돼라!!!~~~~~~~~")
                .content("콘탠트입니다. 제발 됬으면 좋겠습니다.")
                .articleTags(List.of(articleTagJava))
                .category(info)
                .user(user)
                .build();
        info.getArticleList().add(article);
        articleTagJava.injectMappingForArticleAndTag(article);
        em.persist(article);

        user.getArticles().add(article);
        em.persist(user);

        Bookmark bookmark = Bookmark.builder().article(article).user(user).build();
        article.getBookmarks().add(bookmark);
        user.getBookmarks().add(bookmark);
        em.persist(bookmark);

        ArticleLike articleLike = ArticleLike.builder().article(article).user(user).build();
        article.getArticleLikes().add(articleLike);
        user.getArticleLikes().add(articleLike);
        em.persist(articleLike);

        Comment comment = Comment.builder().article(article).content("내용니ㅐ야너ㅐㅓ랸ㅇㄴㅇㄴㄹ")
                .user(user).build();
        em.persist(comment);
        article.getComments().add(comment);


        CommentDto.Response commentDto = CommentDto.Response.builder().avatar(AvatarDto.SimpleResponse.of(avatar))
                .articleId(article.getId())
                .commentId(comment.getId())
                .content(comment.getContent())
                .userInfo(UserDto.ResponseSimpleUserDto.of(user))
                .avatar(AvatarDto.SimpleResponse.of(avatar))
                .build();
        Long id = userRepository.findUserByEmail(EMAIL1).orElseThrow().getId();

        String accessToken = jwtTokenUtil.createAccessToken(EMAIL1, id, ROLE_USER_LIST);

    //when
        ResultActions perform = mockMvc.perform(
                get("/articles/" + id)
                        .header(JWT_HEADER, accessToken)
        );

        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.articleId").value(article.getId()))
                .andExpect(jsonPath("$.title").value(article.getTitle()))
                .andExpect(jsonPath("$.content").value(article.getContent()))
                .andExpect(jsonPath("$.clicks").value(article.getClicks()))
                .andExpect(jsonPath("$.likes").value(1))
                .andExpect(jsonPath("$.isClosed").value(false))
                .andExpect(jsonPath("$.isLiked").value(true))
                .andExpect(jsonPath("$.isBookmarked").value(true))
                .andExpect(jsonPath("$.tags[0:1].tagId").value(1))
                .andExpect(jsonPath("$.tags[0:1].name").value("JAVA"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.lastModifiedAt").exists())
                .andExpect(jsonPath("$.expiredAt").doesNotExist())
                .andExpect(jsonPath("$.userInfo.userId").value(id))
                .andExpect(jsonPath("$.userInfo.nickname").value("nickname"))
                .andExpect(jsonPath("$.userInfo.grade").value("BRONZE"))
                .andExpect(jsonPath("$.avatar.avatarId").value(avatar.getId()))
                .andExpect(jsonPath("$.avatar.filename").value(avatar.getOriginalFilename()))
                .andExpect(jsonPath("$.avatar.remotePath").value(avatar.getRemotePath()))
                .andExpect(jsonPath("$.comments[0:1].articleId").value(commentDto.getArticleId().intValue()))
                .andExpect(jsonPath("$.comments[0:1].commentId").value(comment.getId().intValue()))
                .andExpect(jsonPath("$.comments[0:1].userInfo.userId").value(user.getId().intValue()))
                .andExpect(jsonPath("$.comments[0:1].userInfo.nickname").value("nickname"))
                .andExpect(jsonPath("$.comments[0:1].userInfo.grade").value("BRONZE"))
                .andExpect(jsonPath("$.comments[0:1].avatar.avatarId").value(avatar.getId().intValue()))
                .andExpect(jsonPath("$.comments[0:1].avatar.filename").value(avatar.getOriginalFilename()))
                .andExpect(jsonPath("$.comments[0:1].avatar.remotePath").value(avatar.getRemotePath()))
                .andExpect(jsonPath("$.comments[0:1].createdAt").exists())
                .andExpect(jsonPath("$.comments[0:1].lastModifiedAt").exists());
    }

    @Test
    @DisplayName("게시글 상세조회시 jwt 토큰을 보내지 않을시 isBookmarked와 isLiked를 flase로 보낸다.")
    public void findDetailArticle_suc2() throws Exception {
        //given
        Tag JAVA = Tag.builder().name(TagName.JAVA).build();
        em.persist(JAVA);

        Category info = Category.builder().name(CategoryName.INFO).build();
        em.persist(info);

        Avatar avatar = Avatar.builder().remotePath("remotePath")
                .originalFilename("fileName")
                .build();
        em.persist(avatar);

        User user = User.builder().email(EMAIL1).nickname("nickname").grade(Grade.BRONZE).avatar(avatar).build();


        ArticleTag articleTagJava = ArticleTag.builder().tag(JAVA).build();



        Article article = Article.builder().title("테스트 타이틀입니다. 잘부탁드립니다. 제발 돼라!!!~~~~~~~~")
                .content("콘탠트입니다. 제발 됬으면 좋겠습니다.")
                .articleTags(List.of(articleTagJava))
                .category(info)
                .user(user)
                .build();
        info.getArticleList().add(article);
        articleTagJava.injectMappingForArticleAndTag(article);
        em.persist(article);

        user.getArticles().add(article);
        em.persist(user);

        Bookmark bookmark = Bookmark.builder().article(article).user(user).build();
        article.getBookmarks().add(bookmark);
        user.getBookmarks().add(bookmark);
        em.persist(bookmark);

        ArticleLike articleLike = ArticleLike.builder().article(article).user(user).build();
        article.getArticleLikes().add(articleLike);
        user.getArticleLikes().add(articleLike);
        em.persist(articleLike);

        Comment comment = Comment.builder().article(article).content("내용니ㅐ야너ㅐㅓ랸ㅇㄴㅇㄴㄹ")
                .user(user).build();
        em.persist(comment);
        article.getComments().add(comment);


        CommentDto.Response commentDto = CommentDto.Response.builder().avatar(AvatarDto.SimpleResponse.of(avatar))
                .articleId(article.getId())
                .commentId(comment.getId())
                .content(comment.getContent())
                .userInfo(UserDto.ResponseSimpleUserDto.of(user))
                .avatar(AvatarDto.SimpleResponse.of(avatar))
                .build();
        Long id = userRepository.findUserByEmail(EMAIL1).orElseThrow().getId();

        String accessToken = jwtTokenUtil.createAccessToken(EMAIL1, id, ROLE_USER_LIST);

        //when
        ResultActions perform = mockMvc.perform(
                get("/articles/" + id)

        );

        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.articleId").value(article.getId()))
                .andExpect(jsonPath("$.title").value(article.getTitle()))
                .andExpect(jsonPath("$.content").value(article.getContent()))
                .andExpect(jsonPath("$.clicks").value(article.getClicks()))
                .andExpect(jsonPath("$.likes").value(1))
                .andExpect(jsonPath("$.isClosed").value(false))
                .andExpect(jsonPath("$.isLiked").value(false))
                .andExpect(jsonPath("$.isBookmarked").value(false))
                .andExpect(jsonPath("$.tags[0:1].tagId").value(JAVA.getId().intValue()))
                .andExpect(jsonPath("$.tags[0:1].name").value("JAVA"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.lastModifiedAt").exists())
                .andExpect(jsonPath("$.expiredAt").doesNotExist())
                .andExpect(jsonPath("$.userInfo.userId").value(id))
                .andExpect(jsonPath("$.userInfo.nickname").value("nickname"))
                .andExpect(jsonPath("$.userInfo.grade").value("BRONZE"))
                .andExpect(jsonPath("$.avatar.avatarId").value(avatar.getId()))
                .andExpect(jsonPath("$.avatar.filename").value(avatar.getOriginalFilename()))
                .andExpect(jsonPath("$.avatar.remotePath").value(avatar.getRemotePath()))
                .andExpect(jsonPath("$.comments[0:1].articleId").value(article.getId().intValue()))
                .andExpect(jsonPath("$.comments[0:1].commentId").value(comment.getId().intValue()))
                .andExpect(jsonPath("$.comments[0:1].userInfo.userId").value(user.getId().intValue()))
                .andExpect(jsonPath("$.comments[0:1].userInfo.nickname").value("nickname"))
                .andExpect(jsonPath("$.comments[0:1].userInfo.grade").value("BRONZE"))
                .andExpect(jsonPath("$.comments[0:1].avatar.avatarId").value(avatar.getId().intValue()))
                .andExpect(jsonPath("$.comments[0:1].avatar.filename").value(avatar.getOriginalFilename()))
                .andExpect(jsonPath("$.comments[0:1].avatar.remotePath").value(avatar.getRemotePath()))
                .andExpect(jsonPath("$.comments[0:1].createdAt").exists())
                .andExpect(jsonPath("$.comments[0:1].lastModifiedAt").exists());

    }
}
