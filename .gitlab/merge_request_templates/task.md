# Overview of Changes:

✏️ Put your description here!

## Merge Request Checklist 🚀 - Task 📝

- [ ] Merge is set to correct branch
- [ ] Reviewer has inspected changes
- [ ] MR meets Task DoD (apart from testing on staging)
- [ ] MR meets NFRs

## Relevant ACs

Put your ACs here: ⬇️

## Manual Testing Spreadsheet Link 🔗 ⤵️

[Manual Test Spreadsheet](https://docs.google.com/spreadsheets/d/1WMRGGxxWY_qZ3evvQZ5MqV_NyMz-aC9aJWcTJF0thDQ/edit?usp=sharing)

## Task DoD

- [ ] All code has been formatted to the style guide
- [ ] All classes/methods have necessary documentation
- [ ] Relevant and useful (as defined in Testing Workshop) Unit, Integration and cucumber tests are
  written and pass
- [ ] Different team member has reviewed merge request
- [ ] All details on the task card on ScrumBoard have been implemented
- [ ] Acceptance criteria and NFRs have been reviewed by the developer before a merge request is
  created, and by the reviewer during the review.
- [ ] Assignee has written and run manual tests for all relevant ACs
- [ ] Manual tests for the relevant ACs have been reviewed by the reviewer for all flows and edge
  cases
- [ ] Any comments by the reviewer of the task have been actioned
- Assignee will run manual tests on staging after merge
- Task branch has been deleted off of EngGit
  <br>

### Non-Functional Requirements

#### NFR 1️⃣ 
- [ ] NFR 1 Met ✅ 
- [ ] NFR 1 N/A ❌<br>
There must be an appropriate amount of sensical data including user accounts to show all
functionality works, i.e. if an AC requires 10 or more items for pagination, then there must
be more than 10 items to show the pagination feature works.
<br> <br>

#### NFR 2️⃣ 
- [ ] NFR 2 Met ✅
- [ ] NFR 2 N/A ❌<br>
The product must maintain a consistent and accessible look and feel. <br>
- a. Colours and fonts must stay consistent across pages, including colours of buttons.
- b. The app must be responsive to different screen sizes – mobile to desktop.
- c. The app must offer a consistent user experience in terms of interactions with menus, buttons, links, or input fields.
<br> <br>

#### NFR 3️⃣
- [ ] NFR 3 Met ✅
- [ ] NFR 3 N/A ❌<br>
The product must be both user-friendly and fool-proof: users must be supported in their
tasks by explicitly highlighting all errors or fields that are invalid and helping users to
correct these mistakes. If there are input mistakes entries/work-done must not be cleared (*except for passwords*).
<br> <br>

#### NFR 4️⃣
- [ ] NFR 4 Met ✅
- [ ] NFR 4 N/A ❌<br>
The product must accept all valid characters, included accentuated letters such as
macrons (e.g., Māori, Müller, ...).

#### NFR 5️⃣
- [ ] NFR 5 Met ✅
- [ ] NFR 5 N/A ❌<br>
When interacting with any highlightable element on the page (e.g., text fields, button),
pressing tab must move the user to the next element in an ordered manner. For example,
pressing tab move down fields on a form, but does not move the cursor randomly between
the different inputs.