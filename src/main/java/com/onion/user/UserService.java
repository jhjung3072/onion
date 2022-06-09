package com.onion.user;

import com.onion.config.Utility;
import com.onion.config.mail.EmailMessage;
import com.onion.config.mail.EmailService;
import com.onion.domain.AuthenticationType;
import com.onion.domain.User;
import com.onion.exception.UserNotFoundException;
import com.onion.paging.PagingAndSortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import javax.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    public static final int USER_PER_PAGE = 10;
    private final UserRepository userRepository;

    private final RoleService roleService;

    private final TemplateEngine templateEngine;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;


    public void listByPage(int pageNum, PagingAndSortingHelper helper) {
        helper.listEntities(pageNum, USER_PER_PAGE, userRepository);
    }


    public void registerUser(User user) {
        // 패스워드 암호화
        encodePassword(user);
        user.addRole(roleService.findByName("ROLE_USER"));
        // 가입 유형은 DATABASE 로 설정
        user.setAuthenticationType(AuthenticationType.DATABASE);

        // 회원 인증 코드 생성 및 등록
        String randomCode = RandomString.make(64);
        user.setEmailCheckToken(randomCode);

        userRepository.save(user);

    }

    // 회원 정보 수정
    public User updateAccount(User userInForm) {
        User userInDB = userRepository.findById(userInForm.getId()).get();

        // 수정폼에 패스워드가 채워져있다면 db에 저장후 암호화
        if (!userInForm.getPassword().isEmpty()) {
            userInDB.setPassword(userInForm.getPassword());
            encodePassword(userInDB);
        }

        // 닉네임 수정
        userInDB.setNickname(userInForm.getNickname());
        return userRepository.save(userInDB);
    }

    // 패스워드 암호화
    private void encodePassword(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
    }

    public User get(Integer id) throws UserNotFoundException {
        try {
            return userRepository.findById(id).get();
        } catch (NoSuchElementException ex) {
            throw new UserNotFoundException("해당 유저ID를 찾을 수 없습니다. ID : ");
        }
    }

    public void delete(Integer id) throws UserNotFoundException {
        Integer countById = userRepository.countById(id);
        if (countById == null || countById == 0) {
            throw new UserNotFoundException("해당 유저ID를 찾을 수 없습니다. ID : " + id);
        }
        userRepository.deleteById(id);
    }

    // 유저 이메일 중복 확인
    // Duplicate = 중복
    // OK = 중복 아님
    public boolean isEmailUnique(Integer id, String email) {
        User userByEmail = userRepository.findByEmail(email);

        // 기존 db에 해당 이메일이 없다면 true
        if (userByEmail == null) return true;

        boolean isCreatingNew = (id == null);

        if (isCreatingNew) { // 새로 생성중인 폼에 이미 이메일이 존재한다면 false
            if (userByEmail != null) return false;
        } else { // 수정중인 폼인데 이메일의 id가 다르다면(개체가 서로 다르다면) false
            if (userByEmail.getId() != id) {
                return false;
            }
        }

        return true;
    }

    public void updateUserEnabledStatus(Integer id, boolean enabled) {
        userRepository.updateEnabledStatus(id, enabled);
    }

    public void sendVerificationEmail(HttpServletRequest request, User newUser) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + newUser.getEmailCheckToken() +
                "&email=" + newUser.getEmail());
        context.setVariable("nickname", newUser.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "오이마켓 회원가입을 완료하시려면 링크를 클릭하세요.");
        context.setVariable("host", Utility.getSiteURL(request));
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newUser.getEmail())
                .subject("오이마켓, 회원가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    public void completeSignUp(User newUser) {
        newUser.completeSignUp();
    }

}
