/*
 * 책임 작성자: 박혜정
 * 최초 작성일: 2022-11-14
 * 최근 수정일: 2022-12-06
 */

type ButtonText = {
  children: string;
  funcProp?: any;
};

export const Button = ({ children, funcProp }: ButtonText) => {
  return (
    <button
      className="bg-main-yellow text-[16px] px-5 py-[6px] rounded-full"
      type="submit"
      onClick={funcProp ? funcProp : null}
    >
      {children}
    </button>
  );
};
