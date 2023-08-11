package com.ecommerce.beta.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecommerce.beta.entity.UserInfo;
import com.ecommerce.beta.service.UserInfoService;
import com.ecommerce.beta.worker.UsernameProvider;

@Controller
@RequestMapping("/dashboard")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class DashboardController {
	
    @Autowired
    UserInfoService userInfoService;

    @Autowired
    UsernameProvider usernameProvider;
   
    
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String users(Model model,
                        String keyword,
                        @PageableDefault(size = 15, sort = "username") Pageable pageable,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "7") int size,
                        @RequestParam(defaultValue = "username") String field,
                        @RequestParam(defaultValue = "ASC") String sort){

        pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort), field));

        Page<UserInfo> users;
        if(keyword == null){
             users = userInfoService.loadAllUsers(pageable);
            model.addAttribute("users",users);
        }
        else{
            users = userInfoService.search(keyword, pageable);
            model.addAttribute("users",users);
        }

        //Pagination Values
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("field", field);
        model.addAttribute("sort", sort);
        model.addAttribute("pageSize", size);
        int startPage = Math.max(0, page - 1);
        int endPage = Math.min(page + 1, users.getTotalPages() - 1);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        model.addAttribute("empty", users.getTotalElements() == 0);

        return "dashboard/html/user/UsersList";
    }

    @GetMapping("/users/{uuid}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String viewUser(@PathVariable UUID uuid, Model model,
                           @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(defaultValue = "createdAt") String field,
                           @RequestParam(defaultValue = "ASC") String sort){

        pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort), field));


        UserInfo user = userInfoService.getUser(uuid);

//        model.addAttribute("deleted",user.isDeleted());
        model.addAttribute("uuid",uuid);
        model.addAttribute("firstName",user.getFirstName());
        model.addAttribute("lastName",user.getLastName());
        model.addAttribute("username",user.getUsername());
        model.addAttribute("email",user.getEmail());
        model.addAttribute("phone",user.getPhone());
        model.addAttribute("role",user.getRole());
        model.addAttribute("enabled",user.isEnabled());

        return "dashboard/html/user/UserDetailView";

    }

    @GetMapping("/users/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String deleteUserById(@PathVariable("id") UUID uuid){

        UserInfo user = userInfoService.getUser(uuid);
//        user.setDeleted(true);
        user.setEnabled(false);
//        user.setDeletedAt(new Date());
//        user.setDeletedBy(usernameProvider.get());

        System.out.println("Soft deleting user"+user.getUsername());
        userInfoService.save(user);

//        userInfoService.delete(uuid);
        return "redirect:/dashboard/users";
    }
//    @PostMapping("/generateReport")
//    //  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
//      @ResponseBody
//      public ResponseEntity<ByteArrayResource> salesReportGenerator(@RequestBody Map<String, Object> requestData ) throws ParseException, IOException, DocumentException {
//          String report = (String) requestData.get("report");
//          String type = (String) requestData.get("type");
//
//          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//          Date fromDate = dateFormat.parse((String) requestData.get("from"));
//          Date toDate = dateFormat.parse((String) requestData.get("to"));
//
//          String generatedFile="";
//          switch (report){
//              case "orders" ->{
//                  List<OrderHistory> orders = orderHistoryService.findOrdersByDate(fromDate, toDate);
//                  if(type.equals("csv")){
//                      generatedFile = reportGenerator.generateOrderHistoryCsv(orders);
//                  }else {
//                      generatedFile = reportGenerator.generateOrderHistoryPdf(orders, (String) requestData.get("from"), (String) requestData.get("to"));
//                  }
//              }
//          }
//          File requestedFile = new File(generatedFile);
//          ByteArrayResource resource = new ByteArrayResource(FileUtils.readFileToByteArray(requestedFile));
//          HttpHeaders headers = new HttpHeaders();
//
//          if(type.equals("csv")){
//              headers.setContentType(MediaType.parseMediaType("text/csv"));
//          }else{
//              headers.setContentType(MediaType.APPLICATION_PDF);
//          }
//          headers.setContentDispositionFormData("attachment", generatedFile);
//          return ResponseEntity.ok()
//                  .headers(headers)
//                  .body(resource);
//
//      }
    
}
